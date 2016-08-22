package com.augmentis.ayp.photogallery;

import android.content.pm.LauncherApps;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Wilailux on 8/16/2016.
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        // เชื่อมต่อ url
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();// คือ data ที่ web ส่งมา

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()+ ": with " + urlSpec);
            }

            int bytesRead = 0;

            byte[] buffer = new byte[2048];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer,0,bytesRead);
            }

            out.close();

            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //
    private static final String FLICKR_URL = "https://api.flickr.com/services/rest/";

    private static final String API_KEY = "e1fa4677f96191d82ba39c9279c6aac4";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    /**
     * search photo then put into <b>items</b>
     *
     * @param items array target
     * @param key to search
     */
    public void searchPhotos (List<GalleryItem> items ,String key) {
        try {
            String url = buildUri(METHOD_SEARCH,key);
            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items,jsonStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

    /**
     * get recent photo then put into items
     *
     * @param items array target
     */
    public void getRecentPhotos (List<GalleryItem> items) {
        try {
        String url = buildUri(METHOD_GET_RECENT);
        String jsonStr = queryItem(url);
        if (jsonStr != null) {
                parseJSON(items,jsonStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

    /**
     * get url
     *
     * @param method
     * @param param
     * @return url
     * @throws IOException
     */
    private String buildUri(String method, String... param) throws IOException {

        String jsonString = null;
        Uri baseUrl = Uri.parse(FLICKR_URL);
        Uri.Builder builder = baseUrl.buildUpon();

//        String url = Uri.parse(FLICKR_URL).buildUpon()
        builder.appendQueryParameter("method", method);
        builder.appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s,url_z");

        if (METHOD_SEARCH.equalsIgnoreCase(method)) {
            builder.appendQueryParameter("text", param[0]);
        }


        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG, "Run URL: " + url);

        return url;
    }

    /**
     * query string url to jsonString
     *
     * @param url
     * @return JSONString
     * @throws IOException
     */
    private String queryItem(String url) throws IOException {
        Log.i(TAG,"Run URL: "+ url);
        String jsonString = getUrlString(url);

        Log.i(TAG,"Received JSON: "+ jsonString);
        return jsonString;
    }

    /**
     * parse Json
     * get photo from photos in jsonBody
     *
     * @param newGalleryItemList ArrayList
     * @param jsonBodyStr String
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr) throws IOException,JSONException {
        JSONObject jsonBody = new JSONObject(jsonBodyStr);// convert String to JSON
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

//        JSONArray photoListJson = new JSONObject(jsonBodyStr).getJSONObject("photos").getJSONArray("photo"); //เหมือน 3 บันทัดข้างบน

        for (int i = 0; i < photoListJson.length(); i++) {

            JSONObject jsonPhotoIem = photoListJson.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(jsonPhotoIem.getString("id"));
            item.setTitle(jsonPhotoIem.getString("title"));

            if(!jsonPhotoIem.has("url_s")) {
                continue;
            }

            item.setUrl(jsonPhotoIem.getString("url_s"));

            if(!jsonPhotoIem.has("url_z")) {
                continue;
            }

            item.setBigSizeUrl(jsonPhotoIem.getString("url_z"));

            newGalleryItemList.add(item);

        }
    }
}
