package com.augmentis.ayp.photogallery;

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
    private static final String FLICKER_URL = "https://api.flickr.com/services/rest/";

    private static final String API_KEY = "e1fa4677f96191d82ba39c9279c6aac4";

    public String fetchItems() throws IOException{
        String jsonString = null;

            String url = Uri.parse(FLICKER_URL).buildUpon()
                    .appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .build().toString();

            jsonString = getUrlString(url);

            Log.i(TAG,"Received JSON: "+ jsonString);
        return jsonString;
    }

    public void fetchItems(List<GalleryItem> items ) {
        try {

        String jsonStr = fetchItems();
        if (jsonStr != null) {
                parseJSON(items,jsonStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to fetch item",e);
        }
    }

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

            newGalleryItemList.add(item);

        }
    }
}
