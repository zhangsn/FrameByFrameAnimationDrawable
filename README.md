FrameByFrameAnimationDrawable
===================


Customized animationdrawable which will not load all anim-list frames into memory at once in order to prevent OutOfMemory error

Frame bitmaps will not be loaded using android build in bitmap loading mechanism which will scale bitmap according to device resolution.

## Usage
1. Write your anim-list xml as usual
2. Use FrameByFrameAnimationDrawable.loadAnimation(Context context, int resId) to create a FrameByFrameAnimationDrawable instance;
3. Use the instance as normal AnimationDrawable
4. Don't forget to call start() to start the animation
