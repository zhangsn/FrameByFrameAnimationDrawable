FrameByFrameAnimationDrawable
===================


Customized AnimationDrawable which will not load all anim-list frames into memory at once in order to prevent OutOfMemory error
## How it works
1. Frame bitmaps will not be loaded using android build in resource loading mechanism which will scale bitmap according to device dpi. The bitmaps are only "scaled" when drawing
2. Only two bitmaps will be in memory at the same time. 
3. Frame bitmaps are preloaded off the UI thread

## Usage
1. Write your anim-list xml as usual
2. Use FrameByFrameAnimationDrawable.loadAnimation(Context context, int resId, boolean extreamlyMemoryLimit) to create a FrameByFrameAnimationDrawable instance;
3. Use the instance as normal AnimationDrawable
4. Don't forget to call start() to start the animation

## About extreamlyMemoryLimit parameter
By default,FrameByFrameAnimationDrawable will cache every frame(only the raw bytes,not bitmap) of the animation-list in memory in order to be I/O efficient when decode raw bytes into bitmap. However if your app has extreamly memory limit, you can set this parameter to true and the cache will not be used.

## Limitation
Cause we don't us Android build in resource loading mechanism, the bitmaps will not be automatically scaled according to difference dpi.You **have to** specify the layout_width and layout_height in real dimentions.
