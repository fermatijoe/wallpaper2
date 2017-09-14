package com.dcs.wallhouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dcs.wallhouse.utils.ExpandListAdapter;
import com.dcs.wallhouse.utils.FavouriteUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //list of methods to use in API calling
    public static List<String> methods = Arrays.asList(
            "newest",
            "highest_rated",
            "search",
            "EarthSat",
            "category",
            "favourites",
            "space",
            "popular"
    );

    //list of categories to show to the user
    public static List<String> categoriesName = Arrays.asList(
            "Abstract",
            "Animal",
            "Anime",
            "Comics",
            "Earth",
            "Fantasy",
            "Man Made",
            "Movie",
            "Music",
            "Photography",
            "Sci Fi",
            "TV Show",
            "Vehicles",
            "Video Game",
            "Women"
    );

    //list of categories to use in API calling
    public static List<String> categoriesId = Arrays.asList(
            "1", //abstract
            "2", //animal
            "3", //anime
            "8", //comics
            "10", //Earth
            "11", //fantasy
            "16", //manmade
            "20", //movie
            "22", //music
            "24", //photography
            "27", //scifi
            "29", //tv show
            "31", //vehicles
            "32", //video game
            "33" //women
    );

    private MaterialSearchView mMaterialSearchView;
    private Toolbar mToolbar;
    private Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("");

        createNavigationDrawer();

        launchListFragment(methods.get(0));

        mMaterialSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        mMaterialSearchView.setCursorDrawable(R.drawable.custom_cursor_search);
        mMaterialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                launchListFragmentForSearch(methods.get(2), query.toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        mMaterialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mMaterialSearchView.setVisibility(View.VISIBLE);
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                mMaterialSearchView.setVisibility(View.GONE);
                //Do some magic
            }
        });

        /*
        MobileAds.initialize(this, "ca-app-pub-9909155562202230~1041762089");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        */



    }


    private void showAboutDialog(){
        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.About)
                .setMessage("Powered By Wallpaper Abyss https://wall.alphacoders.com")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void createNavigationDrawer(){
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.nav_background)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withSelectionListEnabledForSingleProfile(false)
                .build();


        //create the drawer and remember the `Drawer` result object
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.Favorites),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.nav_newest),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.Popular),
                        new PrimaryDrawerItem().withIdentifier(3).withName(R.string.Earth_by_satellite),
                        new ExpandableDrawerItem().withName(R.string.Categories).withIcon(R.drawable.ic_more_horiz_black_24dp).withIdentifier(10).withSelectable(false).withSubItems(
                                new SecondaryDrawerItem().withName(R.string.Abstract).withLevel(2).withIdentifier(101),
                                new SecondaryDrawerItem().withName(R.string.Animal).withLevel(2).withIdentifier(102),
                                new SecondaryDrawerItem().withName(R.string.Anime).withLevel(2).withIdentifier(103),
                                new SecondaryDrawerItem().withName(R.string.Comics).withLevel(2).withIdentifier(104),
                                new SecondaryDrawerItem().withName(R.string.Earth).withLevel(2).withIdentifier(105),
                                new SecondaryDrawerItem().withName(R.string.Fantasy).withLevel(2).withIdentifier(106),
                                new SecondaryDrawerItem().withName(R.string.Man_Made).withLevel(2).withIdentifier(107),
                                new SecondaryDrawerItem().withName(R.string.Movie).withLevel(2).withIdentifier(108),
                                new SecondaryDrawerItem().withName(R.string.Music).withLevel(2).withIdentifier(109),
                                new SecondaryDrawerItem().withName(R.string.Photography).withLevel(2).withIdentifier(1010),
                                new SecondaryDrawerItem().withName(R.string.Sci_Fi).withLevel(2).withIdentifier(1011),
                                new SecondaryDrawerItem().withName(R.string.Space).withLevel(2).withIdentifier(1012),
                                new SecondaryDrawerItem().withName(R.string.TV_Show).withLevel(2).withIdentifier(1013),
                                new SecondaryDrawerItem().withName(R.string.Vehicles).withLevel(2).withIdentifier(1014),
                                new SecondaryDrawerItem().withName(R.string.Video_Game).withLevel(2).withIdentifier(1015))

                )
                .addStickyDrawerItems(new SecondaryDrawerItem().withIdentifier(5).withName(R.string.About))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //always check for null draweritems for examples click on header
                        if (drawerItem != null) {
                            if(drawerItem.getIdentifier() == 1){
                                launchListFragment(methods.get(0)); //newest
                            } else if (drawerItem.getIdentifier() == 2){
                                launchListFragment(methods.get(7)); //popular
                            }else if (drawerItem.getIdentifier() == 3){
                                launchListFragment(methods.get(3)); //earth by satellite
                            }else if(drawerItem.getIdentifier() == 4){
                                launchListFragment(methods.get(5));//favorites
                            }else if(drawerItem.getIdentifier() == 5){
                                showAboutDialog();//about
                            } else if (drawerItem.getIdentifier() == 101){
                                launchListFragmentForCategory(methods.get(4), "1"); //abstract
                            }else if (drawerItem.getIdentifier() == 102){
                                launchListFragmentForCategory(methods.get(4), "2"); //animal
                            }else if (drawerItem.getIdentifier() == 103){
                                launchListFragmentForCategory(methods.get(4), "3"); //anime
                            }else if (drawerItem.getIdentifier() == 104){
                                launchListFragmentForCategory(methods.get(4), "8"); //comics
                            }else if (drawerItem.getIdentifier() == 105){
                                launchListFragmentForCategory(methods.get(4), "10"); //earth
                            }else if (drawerItem.getIdentifier() == 106){
                                launchListFragmentForCategory(methods.get(4), "11"); //fantasy
                            }else if (drawerItem.getIdentifier() == 107){
                                launchListFragmentForCategory(methods.get(4), "16"); //manmade
                            }else if (drawerItem.getIdentifier() == 108){
                                launchListFragmentForCategory(methods.get(4), "20"); //movie
                            }else if (drawerItem.getIdentifier() == 109){
                                launchListFragmentForCategory(methods.get(4), "22"); //music
                            }else if (drawerItem.getIdentifier() == 1010){
                                launchListFragmentForCategory(methods.get(4), "24"); //photography
                            }else if (drawerItem.getIdentifier() == 1011){
                                launchListFragmentForCategory(methods.get(4), "27"); //scifi
                            }else if (drawerItem.getIdentifier() == 1012){
                                launchListFragmentForSearch(methods.get(2), "space"); //space
                            }else if (drawerItem.getIdentifier() == 1013){
                                launchListFragmentForCategory(methods.get(4), "29"); //tv show
                            }else if (drawerItem.getIdentifier() == 1014){
                                launchListFragmentForCategory(methods.get(4), "31"); //veichles
                            }else if (drawerItem.getIdentifier() == 1015){
                                launchListFragmentForCategory(methods.get(4), "32");  //video games
                            }
                        }
                        return true;
                    }
                })
                .build();
        mDrawer.setSelection(1);
    }



    @Override
    public void onBackPressed() {
        boolean isDrawerOpen = mDrawer.isDrawerOpen();
        boolean isSearchOpen = mMaterialSearchView.isSearchOpen();

        if(isDrawerOpen){
            mDrawer.closeDrawer();
        }else if(isSearchOpen){
            closeSearch();
        }else {
            super.onBackPressed();
        }
    }

    private void closeSearch(){
        mMaterialSearchView.closeSearch();
        mMaterialSearchView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        mMaterialSearchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_search){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void launchListFragment(String scope){
        getSupportFragmentManager().popBackStack("detail", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment newDetail = ListFragment.newInstance(scope);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newDetail, "LIST_F_TAG")
                .commit();
        if(mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        }
    }
    private void launchListFragmentForCategory(String scope, String catId){
        Log.v("MainActivity", "launching listF with catId: " + catId);
        getSupportFragmentManager().popBackStack("detail", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment newDetail = ListFragment.newInstance(scope, catId, -1);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newDetail, "LIST_F_TAG")
                .commit();
        if(mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        }
    }
    private void launchListFragmentForSearch(String scope, String query){
        getSupportFragmentManager().popBackStack("detail", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment newDetail = ListFragment.newInstance(scope, query);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newDetail, "LIST_F_TAG")
                .commit();
        if(mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        }
    }


}
