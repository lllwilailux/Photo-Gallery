package com.augmentis.ayp.photogallery;

import android.net.Uri;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class GalleryItem {

    private String mId;
    private String mTitle;
    private String mUrl;
    private String mOwner;
    private String mBigSizeUrl;
    private String mLat;
    private String mLon;

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName() {
        return getTitle();
    }

    public void setName(String name) {
        setTitle(name);
    }

    public String getLat() {
        return mLat;
    }

    public void setLat(String mLat) {
        this.mLat = mLat;
    }

    public String getLon() {
        return mLon;
    }

    public void setLon(String mLon) {
        this.mLon = mLon;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof GalleryItem) {
            //is GalleryItem too!
            GalleryItem that = (GalleryItem) o;

            return that.mId != null && mId != null && that.mId.equals(mId);
        }
        return false;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setBigSizeUrl(String mBigSizeUrl) {
        this.mBigSizeUrl = mBigSizeUrl;
    }

    public String getBigSizeUrl() {
        return mBigSizeUrl;
    }


    private static final String PHOTO_URL_PREFIX = "https://www.flickr.com/photos/";

    public Uri getPhotoUri(){
        return Uri.parse(PHOTO_URL_PREFIX).buildUpon() // Return builder
                .appendPath(mOwner)
                .appendPath(mId)
                .build(); // Return Uri
    }

    public boolean isGeoCorrect() {
        return !("0".equals(mLat) && "0".equals(mLon));
    }
}
