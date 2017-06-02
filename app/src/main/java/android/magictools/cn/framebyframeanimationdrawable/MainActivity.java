package android.magictools.cn.framebyframeanimationdrawable;

import android.app.Activity;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.magictools.cn.frameanimationdrawable.FrameByFrameAnimationDrawable;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    Animatable animatable;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageview);
        animatable = (Animatable) getDrawable(R.drawable.animation);
        imageView.setImageDrawable((AnimationDrawable)animatable);
        animatable.start();
        ((RadioGroup)findViewById(R.id.radio)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                animatable.stop();
                switch (checkedId){
                    case R.id.one:
                        animatable = (Animatable) getDrawable(R.drawable.animation);
                        break;
                    case R.id.two:
                        animatable = FrameByFrameAnimationDrawable.loadAnimation(MainActivity.this,R.drawable.animation,false);
                        break;
                    case R.id.three:
                        animatable = FrameByFrameAnimationDrawable.loadAnimation(MainActivity.this,R.drawable.animation,true);
                        break;
                }
                imageView.setImageDrawable((Drawable)animatable);
                animatable.start();
            }
        });
    }
}
