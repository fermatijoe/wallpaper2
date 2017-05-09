package com.dcs.wallhouse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.widget.ImageView;

final public class FadingDrawable extends BitmapDrawable {
    // Only accessed from main thread.
    private static final float FADE_DURATION = 1000; //ms
    private final float density;
    Drawable placeholder;
    long startTimeMillis;
    boolean animating;
    int alpha = 0xFF;

    FadingDrawable(Context context, Bitmap bitmap, Drawable placeholder) {
        super(context.getResources(), bitmap);

        this.density = context.getResources().getDisplayMetrics().density;

        this.placeholder = placeholder;
        animating = true;
        startTimeMillis = SystemClock.uptimeMillis();
    }

    /**
     * Create or update the drawable on the target {@link android.widget.ImageView} to display the supplied bitmap
     * image.
     */
    static public void setBitmap(ImageView target, Context context, Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            Drawable placeholder = target.getDrawable();
            if (placeholder instanceof AnimationDrawable) {
                ((AnimationDrawable) placeholder).stop();
            }
            FadingDrawable drawable = new FadingDrawable(context, bitmap, placeholder);

            //this will avoid OverDraw
            //target.setBackgroundDrawable(null);
            //target.setBackgroundColor(0);

            target.setImageDrawable(drawable);

        }
    }

    /**
     * Create or update the drawable on the target {@link android.widget.ImageView} to display the supplied
     * placeholder image.
     */
    static void setPlaceholder(ImageView target, Drawable placeholderDrawable) {
        target.setImageDrawable(placeholderDrawable);
        if (target.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) target.getDrawable()).start();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!animating) {
            super.draw(canvas);
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                placeholder = null;
                super.draw(canvas);
            } else {
                if (placeholder != null) {
                    placeholder.draw(canvas);
                }

                int partialAlpha = (int) (alpha * normalized);
                super.setAlpha(partialAlpha);
                super.draw(canvas);
                super.setAlpha(alpha);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    invalidateSelf();
                }
            }
        }


    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        if (placeholder != null) {
            placeholder.setAlpha(alpha);
        }
        super.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (placeholder != null) {
            placeholder.setColorFilter(cf);
        }
        super.setColorFilter(cf);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (placeholder != null) {
            placeholder.setBounds(bounds);
        }
        super.onBoundsChange(bounds);
    }
}