package com.dcs.wallhouse.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.Voice;
import android.util.Log;

import com.dcs.wallhouse.R;
import com.dcs.wallhouse.model.Wallpaper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class WallpaperTask extends AsyncTask<String, Void, List<Wallpaper>> {
    private static final String LOG_TAG = WallpaperTask.class.getSimpleName();

    private List<Wallpaper> getDataFromJson(String jsonStr) throws JSONException {

        try {
            //add if control in case of error // TODO: 03/05/2017
            List<Wallpaper> wallResults = new ArrayList<>();
            JSONObject rootJSON = new JSONObject(jsonStr);
            if(rootJSON.getString("success").equals("true")) {
                JSONArray imagesArray = rootJSON.getJSONArray("wallpapers");
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject image = imagesArray.getJSONObject(i);
                    if (image.getString("file_type").equals("jpg")
                            && !image.getString("category").equals("Anime")
                            && !image.getString("category").equals("Music")
                            && !image.getString("category").equals("Celebrity")
                            && !image.getString("category").equals("Video Game")
                            && !image.getString("category").equals("Women")
                            && !image.getString("category").equals("TV Show")) {
                        String res = image.getString("width") + "*" + image.getString("height");
                        Wallpaper wallpaper = new Wallpaper(
                                image.getString("id"),
                                image.getString("url_image"),
                                image.getString("url_thumb"),
                                res);
                        wallResults.add(wallpaper);
                    }
                }
                if(wallResults.isEmpty() || wallResults == null){
                    //if every wall was blacklisted then add just one
                    //so that more pages can be loaded after
                    JSONObject image = imagesArray.getJSONObject(0);
                    String res = image.getString("width") + "*" + image.getString("height");
                    Wallpaper wallpaper = new Wallpaper(
                            image.getString("id"),
                            image.getString("url_image"),
                            image.getString("url_thumb"),
                            res);
                    wallResults.add(wallpaper);
                }
            }
            return wallResults;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Problem parsing JSON", e);
        }
        return null;
    }


    //params 0 is method (newest, hig_rating, search, category)
    //params 1 is page to load (first page automatically shown has n.1)
    //params 2 is whether the displayed list of walls should be replaced with this query UNUSED
    @Override
    protected List<Wallpaper> doInBackground(String... params) {
        if (params[0] == null) {
            Log.e(LOG_TAG, "Id passed in was null");
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        try {
            final String BASE_URL =
                    "https://wall.alphacoders.com/api2.0/get.php?auth=68300e070409b2fe66caf7b80bdb4502";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("method", params[0])
                    .appendQueryParameter("info_level", "2")
                    .appendQueryParameter("width", "3000") //limits for quicker loading
                    .appendQueryParameter("height", "3000") //limits for quicker loading
                    .appendQueryParameter("operator", "max") //limits for quicker loading
                    .appendQueryParameter("page", params[1])
                    .build();
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "built url: " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getDataFromJson(jsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
