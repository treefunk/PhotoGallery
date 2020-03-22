package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//7ae871b7f815a6bcecbe9b60a512ca5e


public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";

    public static final String API_KEY = "7ae871b7f815a6bcecbe9b60a512ca5e";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage());
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(){

        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras","url_s")
                    .build().toString();

            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items,jsonBody);

            Log.i(TAG,"Received JSON: " + jsonString);
        }catch (IOException e){
            Log.e(TAG,"Failed to fetch items", e);
        }catch (JSONException je){
            Log.e(TAG, "failed to parse json", je);
        }

        return items;

    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new Gson().fromJson(photoJsonObject.toString(),GalleryItem.class);
/*            GalleryItem item = new GalleryItem();

            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));*/
            items.add(item);
        }
    }


}
