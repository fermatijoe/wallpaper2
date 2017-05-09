package com.dcs.wallhouse.model;

public class Wallpaper {
    private String mId;
    private String mUrl;
    private String mPreview;
    private String mResolution;

    public Wallpaper(String id, String url, String preview, String resolution){
        mId = id;
        mUrl = url;
        mPreview = preview;
        mResolution = resolution;
    }

    public String getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPreview() {
        return mPreview;
    }
}
