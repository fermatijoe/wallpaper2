package com.dcs.wallhouse;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dcs.wallhouse.model.Wallpaper;
import com.dcs.wallhouse.tasks.WallpaperTask;
import com.dcs.wallhouse.utils.EndlessRecyclerViewScrollListener;
import com.dcs.wallhouse.utils.FavouriteUtils;
import com.dcs.wallhouse.utils.SpacesItemDecoration;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;

import static android.R.attr.textSelectHandleRight;
import static android.R.attr.transitionName;
import static android.R.attr.value;
import static android.R.id.list;
import static com.dcs.wallhouse.R.id.recyclerView;

public class ListFragment extends Fragment {
    private static final String LOG_TAG = ListFragment.class.getSimpleName();
    private static final String ARG_SCOPE = "com.dcs.wallhouse.scope";
    private static final String ARG_QUERY = "com.dcs.wallhouse.query";
    private static final String CATEGORY_NAME = "com.dcs.wallhouse.query";

    private TextView mTextView;
    private ProgressBar mProgressBar;
    public String mMethod, mQuery, mCategory;
    private WallAdapter mWallAdapter;
    private RecyclerView mRecyclerView;

    public static ListFragment newInstance(String scope) {
        Bundle args = new Bundle();
        args.putString(ARG_SCOPE, scope);
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ListFragment newInstance(String scope, String query) {
        Bundle args = new Bundle();
        args.putString(ARG_SCOPE, scope);
        args.putString(ARG_QUERY, query);
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ListFragment newInstance(String scope, String categoryname, int Null) {
        Bundle args = new Bundle();
        args.putString(ARG_SCOPE, scope);
        args.putString(CATEGORY_NAME, categoryname);
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMethod = getArguments().getString(ARG_SCOPE);
        if(getArguments().getString(ARG_QUERY) != null){
            mQuery = getArguments().getString(ARG_QUERY);
        }
        if(getArguments().getString(CATEGORY_NAME) != null){
            mCategory = getArguments().getString(CATEGORY_NAME);
        }

        Log.v(LOG_TAG, "listF loaded with\n" + "mMethod: " + mMethod +"\nmQuery: " + mQuery
                + "\nmCategory: " + mCategory);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);


        mWallAdapter = new WallAdapter(new ArrayList<Wallpaper>());
        mRecyclerView = (RecyclerView) rootView.findViewById(recyclerView);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(glm);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(16, getActivity()));
        mRecyclerView.setAdapter(mWallAdapter);
        mRecyclerView.setHasFixedSize(true);


        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(glm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if(totalItemsCount >= 1){
                    if(mQuery == null){
                        if(mMethod.equals("EarthSat")){
                            showEarthWallpapers(true);
                        }else if (mMethod.equals("space")){

                        }else {
                            loadNextPage(page, mMethod, null, mCategory);
                        }

                    }else {
                        loadNextPage(page, mMethod, mQuery, mCategory);
                    }

                }
            }
        });



        mTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progress_view);

        if(checkConnectivity()){
            if(mQuery != null){
                new Async1().execute(mMethod, "1", mQuery);
            }
            if(mMethod.equals("EarthSat")){
                showEarthWallpapers(false);
            }else if(mMethod.equals("category")){
                new Async1().execute(mMethod, "1", null, mCategory);
            }else if(mMethod.equals("favourites")){
                List<Wallpaper> favList = FavouriteUtils.getAllFavorites();
                if (favList.size() == 0) {
                    mProgressBar.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText("No favorites");
                    Toasty.warning(getActivity(), "No favs").show();
                } else {
                    mWallAdapter.addItemsToList(favList, false);
                    mProgressBar.setVisibility(View.GONE);
                }
            } else {
                new Async1().execute(mMethod, "1", null);
            }

        }else {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText("No internet connection");
            mProgressBar.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void loadNextPage(int currentPage, String method){
        new Async1().execute(method,
                Integer.valueOf(currentPage).toString());
    }

    //space wallpaper is not a proper category insetad we are just doing a search
    private void loadSpaceNextPage(){

    }


    private void loadNextPage(int currentPage, String method, String query, String catId){
        if(query != null){
            if(catId != null && mMethod.equals("category")){
                new Async1().execute(method,
                        Integer.valueOf(currentPage).toString(),
                        query,
                        catId);
            }else {
                new Async1().execute(method,
                        Integer.valueOf(currentPage).toString(),
                        query);
            }
        }else {
            if(!mMethod.equals("favourites")) {
                new Async1().execute(method,
                        Integer.valueOf(currentPage).toString(),
                        null);
            }
        }
    }

    private class Async1 extends WallpaperTask {
        @Override
        protected void onPostExecute(List<Wallpaper> list) {
            if(list != null && list.size() != 0){
                mWallAdapter.addItemsToList(list, true);
            }else {

                Log.e(LOG_TAG, "Wallpaper list is null");
            }
            mProgressBar.setVisibility(View.GONE);

        }
    }


    private boolean checkConnectivity(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private class WallHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView mImageView;
        public TextView mTextView;

        public WallHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.grid_item_image);
            mTextView = (TextView) itemView.findViewById(R.id.grid_item_title);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = WallHolder.this.getAdapterPosition();
            Wallpaper w = mWallAdapter.getList().get(adapterPosition);


            mRecyclerView.smoothScrollToPosition(adapterPosition);
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // Open activity here.
                    if(getActivity() != null){
                        String url = mWallAdapter.getList().get(WallHolder.this.getAdapterPosition()).getUrl();
                        String preview_url = mWallAdapter.getList().get(WallHolder.this.getAdapterPosition()).getPreview();
                        String resolution = mWallAdapter.getList().get(WallHolder.this.getAdapterPosition()).getResolution();

                        Log.v(LOG_TAG, "Loading detailActivity with url: " + url);

                        Gson gson = new Gson();
                        String serialized = gson.toJson(mWallAdapter.getList().get(WallHolder.this.getAdapterPosition()));

                        Intent myIntent = new Intent(getActivity(), DetailActivity.class);
                        myIntent.putExtra("url", url);
                        myIntent.putExtra("preview_url", preview_url);
                        myIntent.putExtra("resolution", resolution);
                        myIntent.putExtra("serialized", serialized);
                        /*
                        myIntent.putExtra("trans_name", ViewCompat.getTransitionName(mImageView));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                getActivity(),
                                mImageView,
                                ViewCompat.getTransitionName(mImageView));
                        getActivity().startActivity(myIntent, options.toBundle());
                        */
                        getActivity().startActivity(myIntent);
                        getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);


                    }
                    return true;
                }
            });


        }
    }




    private class WallAdapter extends RecyclerView.Adapter<WallHolder> {
        private List<Wallpaper> mWallpapers;

        public WallAdapter(List<Wallpaper> wallpapers) {
            mWallpapers = wallpapers;
        }

        public void clear() {
            mWallpapers.clear();
            notifyDataSetChanged();

        }
        public void add(Wallpaper wallpaper){
            mWallpapers.add(wallpaper);
            notifyDataSetChanged();
        }
        public void addItemsToList(List<Wallpaper> newWallpapers, boolean append){
            if(append){
                mWallpapers.addAll(newWallpapers);
                Log.v(LOG_TAG, "Added " + newWallpapers.size() + " elememts to the adapter");
            }else {
                mWallpapers = newWallpapers;
            }
            mRecyclerView.post(new Runnable() {
                public void run() {
                    mWallAdapter.notifyDataSetChanged();
                }
            });

        }

        public List<Wallpaper> getList(){
            return mWallpapers;
        }

        @Override
        public WallHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View rootView = inflater.inflate(R.layout.list_item_row, parent, false);
            return new WallHolder(rootView);
        }
        @Override
        public void onBindViewHolder(WallHolder holder, final int position) {
            final Wallpaper currentWallpaper = mWallpapers.get(position);

            ViewCompat.setTransitionName(holder.mImageView, currentWallpaper.getWallId());

            String resizedUrl = currentWallpaper.getUrl();
            resizedUrl = resizedUrl.replace("https://", "");
            resizedUrl = "https://i.scaley.io/500-max/" + resizedUrl;
            Glide.with(getActivity())
                    .load(resizedUrl)
                    .crossFade(500)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.e(LOG_TAG, "GLIDE IMAGE LOAD EXCEPTION");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(holder.mImageView);



        }
        @Override
        public int getItemCount() {
            return mWallpapers.size();
        }
    }

    private void showEarthWallpapers(boolean append){
        List<Wallpaper> walls = getEarthWallpaperList();
        if(walls != null && walls.size() != 0){
            mWallAdapter.addItemsToList(walls, append);
        }else {
            Log.e(LOG_TAG, "Wallpaper list is null");
        }
        mProgressBar.setVisibility(View.GONE);
    }

    public List<Wallpaper> getEarthWallpaperList() {
        String json = null;
        List<Wallpaper> wallResults;
        try {
            InputStream is = getActivity().getAssets().open("earthjson.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            wallResults = new ArrayList<>();
            JSONArray rootJSON = new JSONArray(json);

            List<Integer> chosenElements = new ArrayList<>();
            for(int n = 0; n < 30; n++){
                Random r = new Random();
                chosenElements.add(r.nextInt(1500));
            }

            for(int x = 0; x < 30; x++){
                JSONObject wallJSON = rootJSON.getJSONObject(chosenElements.get(x));
                Wallpaper newWall = new Wallpaper("",
                        wallJSON.getString("image"),
                        "",
                        "1800x1200");
                wallResults.add(newWall);
            }
            return wallResults;

        }catch (JSONException e){
            Log.e(LOG_TAG, "Problem parsing JSON", e);
        }
        return null;
    }
}
