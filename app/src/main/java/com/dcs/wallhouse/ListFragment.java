package com.dcs.wallhouse;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dcs.wallhouse.model.Wallpaper;
import com.dcs.wallhouse.tasks.WallpaperTask;
import com.dcs.wallhouse.utils.EndlessRecyclerViewScrollListener;
import com.dcs.wallhouse.utils.SpacesItemDecoration;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.textSelectHandleRight;
import static android.R.attr.transitionName;
import static android.R.attr.value;
import static com.dcs.wallhouse.R.id.recyclerView;

public class ListFragment extends Fragment {
    private static final String LOG_TAG = ListFragment.class.getSimpleName();
    private static final String ARG_SCOPE = "com.dcs.wallhouse.scope";

    private TextView mTextView;
    private ProgressBar mProgressBar;
    public String mMethod;
    private WallAdapter mWallAdapter;
    private RecyclerView mRecyclerView;

    public static ListFragment newInstance(String scope) {
        Bundle args = new Bundle();
        args.putString(ARG_SCOPE, scope);
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
                    loadNextPage(page, mMethod);
                }
            }
        });

        mTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progress_view);

        if(checkConnectivity()){
            new Async1().execute(mMethod, "1");
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

            String url = mWallAdapter.getList().get(adapterPosition).getUrl();
            String preview_url = mWallAdapter.getList().get(adapterPosition).getPreview();


            if(getActivity() != null){
                Log.v(LOG_TAG, "Loading detailActivity with url: " + url);


                Intent myIntent = new Intent(getActivity(), DetailActivity.class);
                myIntent.putExtra("url", url);
                myIntent.putExtra("preview_url", preview_url);
                /*
                Define the view that the animation will start from
                View viewStart = view.findViewById(R.id.grid_item_image);
                View decor = getActivity().getWindow().getDecorView();
                these appear to be null
                View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                View navBar = decor.findViewById(android.R.id.navigationBarBackground);
                View actionBar = decor.findViewById(R.id.action_bar_container);

                Pair<View, String> p1 = Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                Pair<View, String> p2 = Pair.create(navBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                Pair<View, String> p3 = Pair.create(actionBar, "actionbar");
                Pair<View, String> p4 = Pair.create(viewStart, "wallpaper_transition");

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                p1,
                                p2,
                                p3,
                                p4);

                ActivityCompat.startActivity(getActivity(), myIntent, options.toBundle());
                */
                getActivity().startActivity(myIntent);
            }


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
            notifyDataSetChanged();
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

            String resizedUrl = currentWallpaper.getUrl();
            resizedUrl = resizedUrl.replace("https://", "https://rsz.io/");
            resizedUrl = resizedUrl + "?width=500";
            Glide.with(getActivity())
                    .load(resizedUrl)
                    .crossFade(500)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(holder.mImageView);

        }
        @Override
        public int getItemCount() {
            return mWallpapers.size();
        }
    }


}
