package com.dcs.wallhouse.model;

import com.orm.SugarRecord;

public class Wallpaper extends SugarRecord<Wallpaper> {
    private String mWallId;
    private String mUrl;
    private String mPreview;
    private String mResolution;

    public Wallpaper(){}


    public Wallpaper(String wallid, String url, String preview, String resolution){
        mWallId = wallid;
        mUrl = url;
        mPreview = preview;
        mResolution = resolution;
    }

    public String getWallId() {
        return mWallId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPreview() {
        return mPreview;
    }

    public String getResolution() {
        return mResolution;
    }
}
