package com.qromarck.reciperu.Utilities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONHelper {

    private static final String TAG = "JSONHelper";

    public void getJSONFromUrl(String urlString, final OnJSONResponseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return;
                    }
                    String jsonString = buffer.toString();
                    JSONObject jsonObject = new JSONObject(jsonString);
                    listener.onJSONResponse(jsonObject);
                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURLException: " + e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: " + e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream: " + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }

    public interface OnJSONResponseListener {
        void onJSONResponse(JSONObject response);
    }
}