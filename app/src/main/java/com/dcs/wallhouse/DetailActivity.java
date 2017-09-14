package com.dcs.wallhouse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.transition.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.dcs.wallhouse.model.Wallpaper;
import com.dcs.wallhouse.utils.FavouriteUtils;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import es.dmoral.toasty.Toasty;
import uk.co.senab.photoview.PhotoView;

import static android.R.attr.mimeType;
import static android.R.attr.name;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;
import static com.bumptech.glide.Glide.with;


public class DetailActivity extends AppCompatActivity {
    private final static String LOG_TAG = "DetailActivity";
    private Wallpaper mWallpaper;
    private String mUrl, mPreviewUrl, mResolution;
    private ImageView mWallpaperImageView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private FloatingActionButton mFullscreenFAB;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Transition.TransitionListener mEnterTransitionListener;
    private Bitmap mWallpaperBitmap;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private String mFilePath;

    //The three action buttons
    private LinearLayout mSaveButton, mSetAsButton, mFavouriteButton, mMainLl;
    private ImageView mSaveIv, mSetAsIv, mFavouriteIv;
    private ProgressBar progressBar;
    private TextView mSaveTv, mFavouriteTv, mSetAsTv;
    private View mView1, mView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //supportPostponeEnterTransition();

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        mPreviewUrl = intent.getStringExtra("preview_url");
        mResolution = intent.getStringExtra("resolution");
        Gson gson = new Gson();
        mWallpaper = gson.fromJson(intent.getStringExtra("serialized"), Wallpaper.class);

        mWallpaperImageView = (ImageView) findViewById(R.id.imageViewToolbar);

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = intent.getStringExtra("trans_name");
            mWallpaperImageView.setTransitionName(imageTransitionName);
        }
        */

        loadImageThumbnailRequest();

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mFullscreenFAB = (FloatingActionButton) findViewById(R.id.fullscreenFAB);
        mFullscreenFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFullscreenFAB.getVisibility() == View.VISIBLE){
                    final Dialog nagDialog = new Dialog(DetailActivity.this,
                            R.style.FullscreenDialogTheme);
                    nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    nagDialog.setCancelable(true);
                    nagDialog.setContentView(R.layout.fullscreen_image);
                    FrameLayout fl = (FrameLayout) nagDialog.findViewById(R.id.fullscreen_framelay);
                    PhotoView ivPreview = (PhotoView)nagDialog.findViewById(R.id.iv_preview_image);
                    if(mWallpaperBitmap != null){
                        ivPreview.setImageBitmap(mWallpaperBitmap);
                    }else {
                        with(DetailActivity.this).load(mUrl).crossFade(500).into(ivPreview);
                    }
                    nagDialog.show();
                    fl.setBackgroundColor(Color.BLACK);

                }}});




        mSaveButton = (LinearLayout)findViewById(R.id.save_ll);
        mSetAsButton = (LinearLayout)findViewById(R.id.set_as_ll);
        mFavouriteButton = (LinearLayout)findViewById(R.id.fav_ll);
        progressBar = (ProgressBar) findViewById(R.id.progress_view_download);
        mMainLl = (LinearLayout) findViewById(R.id.main_buttons_ll);

        mFavouriteIv = (ImageView)findViewById(R.id.imageViewFavourite);
        mSaveIv = (ImageView)findViewById(R.id.imageViewDownload); //use progressbar
        mSaveTv = (TextView)findViewById(R.id.m_save_tv);
        mSetAsIv = (ImageView)findViewById(R.id.imageViewSetAs);
        mSetAsTv = (TextView)findViewById(R.id.set_as_tv);
        mFavouriteTv = (TextView)findViewById(R.id.fav_tv);
        mView1 = (View)findViewById(R.id.view1);
        mView2 = (View)findViewById(R.id.view2);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                mSaveIv.setVisibility(View.INVISIBLE);
                fetchImage("save");

            }});

        mSetAsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchImage("setAs");

            }
        });


        mFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FavouriteUtils.checkIfThisIsFavorite(mWallpaper)){
                    //movie is in fav, remove it
                    mFavouriteIv.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    FavouriteUtils.removeThisFromFavorites(mWallpaper);
                } else if(!FavouriteUtils.checkIfThisIsFavorite(mWallpaper)){
                    //movie not in fav, add it
                    mFavouriteIv.setImageResource(R.drawable.ic_favorite_black_24dp);
                    FavouriteUtils.addThisToFavorites(mWallpaper);
                }
            }
        });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_out_down, R.anim.slide_in_down);
    }

    @Override
    protected void onResume() {
        if(FavouriteUtils.checkIfThisIsFavorite(mWallpaper)) {
            Log.v(LOG_TAG, "Wallpaper already in fav");
            mFavouriteIv.setImageResource(R.drawable.ic_favorite_black_24dp);
        }else if(!FavouriteUtils.checkIfThisIsFavorite(mWallpaper)) {
            Log.v(LOG_TAG, "Wallpaper not in fav");
            mFavouriteIv.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
        super.onResume();
    }

    private void fetchImage(String why){
        try{
            boolean hasPermission = (ContextCompat.checkSelfPermission(DetailActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(DetailActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }else{
                saveImage();
            }
        }finally {
            if(mFilePath != null){
                if(why.equals("save")){
                    //toasts are soooo un-material
                    //Toasty.success(DetailActivity.this, "Image saved").show();

                    View parentLayout = findViewById(R.id.fullscreenFAB);
                    Snackbar.make(parentLayout, mResolution, Snackbar.LENGTH_LONG)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }else if(why.equals("setAs")){
                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    String ext = mFilePath.substring(mFilePath.lastIndexOf('.') + 1);
                    String mimeType = map.getMimeTypeFromExtension(ext);
                    Uri uri = Uri.fromFile(new File(mFilePath));
                    Log.v(LOG_TAG, "uri is: " + uri + "\nmimeType is: " + mimeType);
                    intent.setDataAndType(uri, mimeType);
                    intent.putExtra("mimeType", mimeType);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Activity activity = (DetailActivity) this;
                    activity.startActivity(Intent.createChooser(intent, "set as"));

                }else if(why.equals("share")){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Uri bitmapUri = Uri.parse(mFilePath);
                    intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    intent.setType("image/*");
                    startActivity(Intent.createChooser(intent, "Share image via..."));
                }
            }else {
                //Image not ready
            }

        }
    }

    private void setAsWallpaper(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    saveImage();
                } else
                {
                    Log.e(LOG_TAG, "Error downloading image");
                }
            }
        }

    }

    private void saveImage(){
        Glide.with(this)
                .load(mUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        progressBar.setVisibility(View.GONE);
                        mSaveIv.setVisibility(View.VISIBLE);
                        try {
                            saveImageToExternal(Long.valueOf(System.currentTimeMillis()).toString(),
                                    resource);

                        }catch (IOException e){
                            e.printStackTrace();
                            Toasty.error(DetailActivity.this,
                                    "Error downloading image").show();
                            mFilePath = null;
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        progressBar.setVisibility(View.GONE);
                        mSaveIv.setVisibility(View.VISIBLE);
                        Log.i(LOG_TAG, "Loading image failed by glide");
                        super.onLoadFailed(e, errorDrawable);
                    }
                });

    }

    private void saveImageToExternal(String imgName, Bitmap finalBitmap) throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/wallpapers");
        if (!myDir.exists()) {
            myDir.mkdir();
        }

        String name = mUrl.substring(mUrl.lastIndexOf("/") + 1);
        name = name.replace(".jpg", "");
        name = name.replace(".png", "");
        name = name + ".jpg";

        File file = new File(myDir, name);
        if(file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            out.close();
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
            mFilePath = file.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Animates FAB to appear out of nowhere
    @SuppressLint("NewApi")
    public void show(FloatingActionButton fab, LinearLayout buttons) {
        fab.animate().cancel();//cancel all animations
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setAlpha(0f);
        fab.setVisibility(View.VISIBLE);
        //values from support lib source code
        fab.animate().setDuration(1000).scaleX(1).scaleY(1).alpha(1)
                .setInterpolator(new LinearOutSlowInInterpolator());


        buttons.animate().cancel();//cancel all animations
        buttons.setScaleX(0f);
        buttons.setScaleY(0f);
        buttons.setAlpha(0f);
        buttons.setVisibility(View.VISIBLE);
        //values from support lib source code
        buttons.animate().setDuration(1000).scaleX(1).scaleY(1).alpha(1)
                .setInterpolator(new LinearOutSlowInInterpolator());

    }

    private void loadImageThumbnailRequest() {


        // pass the request as a a parameter to the thumbnail request
        Glide.with(this)
                .load(mUrl)
                .asBitmap()
                .thumbnail(
                        with(this)
                        .load(mPreviewUrl)
                        .asBitmap()
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                supportStartPostponedEnterTransition();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                supportStartPostponedEnterTransition();
                                return false;
                            }
                        }))
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        show(mFullscreenFAB, mMainLl);

                        return false;
                    }
                })
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {

                        mWallpaperImageView.setImageBitmap(resource);
                        createPaletteAsync(resource);
                        mWallpaperBitmap = resource;

                    }
                });
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {

            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    // Generate palette asynchronously
    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap)
                .maximumColorCount(12)
                .generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette p) {
                        int colourDominant, colourFAB, colourText, colourTitle;

                        Palette.Swatch swatchDominant = p.getDominantSwatch();
                        colourDominant = swatchDominant.getRgb();
                        List<Palette.Swatch> swatches = p.getSwatches();
                        if(swatches.get(0) != null){
                            Palette.Swatch vibrant = p.getLightMutedSwatch();
                            if(vibrant !=null) {
                                colourFAB = vibrant.getRgb();
                                colourText = swatchDominant.getBodyTextColor();
                                colourTitle = swatchDominant.getTitleTextColor();
                            }else {
                                colourFAB = swatches.get(0).getRgb();
                                colourText = swatches.get(0).getBodyTextColor();
                                colourTitle = swatches.get(0).getTitleTextColor();
                            }
                        }else {
                            colourFAB = getResources().getColor(R.color.colorPrimary);
                            colourText = Color.WHITE;
                            colourTitle = Color.WHITE;
                        }
                        setColour(colourDominant, colourFAB, colourText, colourTitle);

                    }
                });
    }

    /*
        colourText is too dark
     */
    public void setColour(int colourDominant, int colourFAB, int colourText, int colourTitle){
        FrameLayout fl = (FrameLayout)findViewById(R.id.frameLay);
        fl.setBackgroundColor(colourDominant);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fullscreenFAB);
        fab.setBackgroundTintList(ColorStateList.valueOf(colourFAB));
        //This changes solor of icons and text labels
        mFavouriteIv.setColorFilter(colourTitle, PorterDuff.Mode.SRC_IN);
        mSaveIv.setColorFilter(colourTitle, PorterDuff.Mode.SRC_IN);
        mSetAsIv.setColorFilter(colourTitle, PorterDuff.Mode.SRC_IN);
        mSaveTv.setTextColor(colourTitle);
        mSetAsTv.setTextColor(colourTitle);
        mFavouriteTv.setTextColor(colourTitle);
        mView1.setBackgroundColor(colourTitle);
        mView2.setBackgroundColor(colourTitle);
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
                fetchImage("share");
                break;
            case R.id.action_info:
                View parentLayout = findViewById(R.id.fullscreenFAB);
                Snackbar.make(parentLayout, mResolution, Snackbar.LENGTH_LONG)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        })
                        .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                        .show();
                break;
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.slide_out_down, R.anim.slide_in_down);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




}
