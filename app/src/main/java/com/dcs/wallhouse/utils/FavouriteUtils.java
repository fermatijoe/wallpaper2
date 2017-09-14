package com.dcs.wallhouse.utils;

import android.util.Log;

import com.dcs.wallhouse.model.Wallpaper;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;


public class FavouriteUtils {
    private final static String WHERE_CLAUSE_ID = "m_url";


    public static void addThisToFavorites(Wallpaper s){
        s.save();
        Log.v("FAVUTILS", "item added from fav");
    }

    public static void removeThisFromFavorites(Wallpaper s){
        getListFromDb(s, WHERE_CLAUSE_ID).get(0).delete();
        Log.v("FAVUTILS", "item removed from fav");
    }

    public static boolean checkIfThisIsFavorite(Wallpaper s){
        List<Wallpaper> results = getListFromDb(s, WHERE_CLAUSE_ID);
        if(results.size() > 0){
            //found match
            return true;
        }else {
            //no match
            return false;
        }
    }
    private static List<Wallpaper> getListFromDb(Wallpaper s, String whereClause){
        //check for unique walls by URL (since earth sat walls doesn't have ids)
        String wallId = s.getUrl();
        return Select.from(Wallpaper.class)
                        .where(Condition.prop(whereClause)
                        .eq(wallId))
                .list();
    }

    public static List<Wallpaper> getAllFavorites(){
        return Wallpaper.listAll(Wallpaper.class);
    }
}
