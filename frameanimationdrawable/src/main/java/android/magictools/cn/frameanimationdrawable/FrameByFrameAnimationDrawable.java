package android.magictools.cn.frameanimationdrawable;

/**
 * Created by zhangsn on 17/6/2.
 */

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhangsn on 17/4/5.
 */

public class FrameByFrameAnimationDrawable extends Drawable implements Animatable,Runnable{
    boolean oneshot;
    Paint paint;
    int pos;
    Rect drawRect = new Rect();
    List<MyFrame> frameList;
    Bitmap currentBitmap;
    Bitmap nextBitmap;
    Context context;
    AsyncTask task;
    private boolean isRunning;

    /**
     * Load a FrameByFrameAnimationDrawable from resource
     * @param context Context
     * @param resId Resource Id of an animation-list
     * @param extreamlyMemoryLimit Use more CPU and IO time to save even more memory
     * @return A new FrameByFrameAnimationDrawable
     */
    public static FrameByFrameAnimationDrawable loadAnimation(Context context,@XmlRes int resId,boolean extreamlyMemoryLimit){
        final List<MyFrame> myFrames = new LinkedList<>();
        boolean oneshot = true;
        XmlResourceParser parser = context.getResources().getXml(resId);
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("item")) {
                        MyFrame myFrame = new MyFrame();
                        for (int i=0; i<parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equals("drawable")) {
                                myFrame.resId = Integer.parseInt(parser.getAttributeValue(i).substring(1));
                                if(!extreamlyMemoryLimit) {
                                    myFrame.frameBytes = toByteArray(context, myFrame.resId);
                                }
                            }
                            else if (parser.getAttributeName(i).equals("duration")) {
                                myFrame.duration = parser.getAttributeIntValue(i, 1000);
                            }
                        }
                        myFrames.add(myFrame);
                    }else if(parser.getName().equals("animation-list")){
                        for (int i=0; i<parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equals("oneshot")) {
                                oneshot = parser.getAttributeBooleanValue(i,true);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        }catch (IOException | XmlPullParserException e){
            return null;
        }
        return new FrameByFrameAnimationDrawable(context,myFrames,oneshot);
    }
    private FrameByFrameAnimationDrawable(Context context, List<MyFrame> frameList, boolean oneshot){
        this.frameList = frameList;
        this.paint = new Paint();
        this.context = context;
        this.oneshot = oneshot;
        currentBitmap = loadBitmap(frameList.get(0));
        nextBitmap = currentBitmap;
        this.frameList.get(0).isReady = true;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        if(visible){
            if(isRunning){
                start();
            }
        }else{
            if(isRunning){
                unscheduleSelf(this);
            }
        }
        return super.setVisible(visible, restart);
    }

    @Override
    public void start() {
        stop();
        isRunning = true;
        pos = 1;
        currentBitmap = loadBitmap(frameList.get(0));
        nextBitmap = currentBitmap;
        this.frameList.get(0).isReady = true;
        invalidateSelf();
        preloadNextFrame();
        scheduleSelf(this,SystemClock.uptimeMillis()+this.frameList.get(0).duration);
    }
    @Override
    public void stop() {
        isRunning = false;
        unscheduleSelf(this);
        invalidateSelf();
        if(task != null){
            task.cancel(true);
        }
    }

    @Override
    public int getIntrinsicHeight() {
        if(currentBitmap != null){
            return currentBitmap.getHeight();
        }else {
            return super.getIntrinsicHeight();
        }
    }

    @Override
    public int getIntrinsicWidth() {
        if(currentBitmap != null){
            return currentBitmap.getWidth();
        }else {
            return super.getIntrinsicWidth();
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }


    @Override
    public void run() {
        if(frameList.get(pos).isReady){
            currentBitmap = nextBitmap;
            if(pos == frameList.size()-1){
                if(!oneshot){
                    scheduleSelf(this, SystemClock.uptimeMillis()+frameList.get(pos).duration);
                    pos = 0;
                    preloadNextFrame();
                }
            }else{
                scheduleSelf(this,SystemClock.uptimeMillis()+frameList.get(pos).duration);
                pos ++;
                preloadNextFrame();
            }
            invalidateSelf();
        }else{
            frameList.get(pos).isReady = true;
        }
    }
    private void preloadNextFrame(){
        nextBitmap = null;
        frameList.get(pos).isReady = false;
        if(task != null){
            task.cancel(true);
        }
        task = new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                if(!isCancelled()){
                    return loadBitmap(frameList.get(pos));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                nextBitmap = bitmap;
                if(frameList.get(pos).isReady){
                    run();
                }else {
                    frameList.get(pos).isReady = true;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    @Override
    public void draw(@NonNull Canvas canvas) {
        if(currentBitmap != null) {
            drawRect.set(0,0,currentBitmap.getWidth(),currentBitmap.getHeight());
            canvas.drawBitmap(currentBitmap, drawRect,getBounds(), paint);
        }
    }
    static byte[] toByteArray(Context context,int resId) {
        InputStream is = null;
        try {
            is = context.getResources().openRawResource(resId);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int read;
            while ((read = is.read(buf)) != -1) {
                bos.write(buf, 0, read);
            }
            return bos.toByteArray();
        }catch (Exception e){
            return null;
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Bitmap loadBitmap(MyFrame frame){
        Bitmap returnBitmap = null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inJustDecodeBounds = false;
        if(frame.frameBytes != null) {
            return BitmapFactory.decodeByteArray(frame.frameBytes, 0, frame.frameBytes.length, opt);
        }else{
            return BitmapFactory.decodeStream(context.getResources().openRawResource(frame.resId));
        }

    }
    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static class MyFrame {
        int duration;
        int resId;
        byte[] frameBytes;
        boolean isReady = false;
    }
}
