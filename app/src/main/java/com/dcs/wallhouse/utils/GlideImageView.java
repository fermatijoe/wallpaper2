package com.dcs.wallhouse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;

public class GlideImageView extends AppCompatImageView {
    public GlideImageView(Context context) {
        this(context, null);
    }

    public GlideImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlideImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable placeholder = getDrawable();
        if (placeholder instanceof AnimationDrawable) {
            ((AnimationDrawable) placeholder).stop();
            Glide.clear(this);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap != null) FadingDrawable.setBitmap(this, getContext(), bitmap);
    }

    public void setImageBitmapWithoutAnimation(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
    }
}
