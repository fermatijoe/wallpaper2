package com.dcs.wallhouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;
import uk.co.senab.photoview.PhotoView;

import static android.R.attr.x;
import static android.R.attr.y;


public class DetailActivity extends AppCompatActivity {
    private final static String LOG_TAG = "DetailActivity";
    private String mUrl, mPreviewUrl;
    private ImageView mWallpaperImageView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private FloatingActionButton mFullscreenFAB;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Transition.TransitionListener mEnterTransitionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        mPreviewUrl = intent.getStringExtra("preview_url");

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWallpaperImageView = (ImageView) findViewById(R.id.imageViewToolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mFullscreenFAB = (FloatingActionButton) findViewById(R.id.fullscreenFAB);


        loadImageThumbnailRequest();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Animates FAB to appear out of nowhere
    @SuppressLint("NewApi")
    public static void show(FloatingActionButton fab) {
        fab.animate().cancel();//cancel all animations
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setAlpha(0f);
        fab.setVisibility(View.VISIBLE);
        //values from support lib source code
        fab.animate().setDuration(1000).scaleX(1).scaleY(1).alpha(1)
                .setInterpolator(new LinearOutSlowInInterpolator());
    }

    private void loadImageThumbnailRequest() {
        // setup Glide request without the into() method
        DrawableRequestBuilder<String> thumbnailRequest = Glide
                .with(this)
                .load(mPreviewUrl);

        // pass the request as a a parameter to the thumbnail request
        Glide
                .with(this)
                .load(mUrl)
                .asBitmap()
                .thumbnail(Glide
                        .with(this)
                        .load(mPreviewUrl)
                        .asBitmap())
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        show(mFullscreenFAB);
                        return false;
                    }
                })
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        mWallpaperImageView.setImageBitmap(resource);
                        createPaletteAsync(resource);

                    }
                });
    }


    // Generate palette asynchronously
    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap)
                .maximumColorCount(8)
                .generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette p) {
                        int colourDominant;
                        int colourFAB;
                        Palette.Swatch swatchDominant = p.getDominantSwatch();
                        colourDominant = swatchDominant.getRgb();
                        List<Palette.Swatch> swatches = p.getSwatches();
                        if(swatches.get(0) != null){
                            Palette.Swatch vibrant = p.getLightMutedSwatch();
                            if(vibrant !=null) {
                                colourFAB = vibrant.getRgb();
                            }else {
                                colourFAB = swatches.get(0).getRgb();
                            }
                        }else {
                            colourFAB = getResources().getColor(R.color.colorPrimary);
                        }
                        setColour(colourDominant, colourFAB);

                    }
                });
    }

    public void setColour(int colourDominant, int colourFAB){
        RelativeLayout fl = (RelativeLayout)findViewById(R.id.frameLay);
        fl.setBackgroundColor(colourDominant);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fullscreenFAB);
        fab.setBackgroundTintList(ColorStateList.valueOf(colourFAB));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.activity_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Toasty.success(this, "Shared!").show();
                break;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




}
