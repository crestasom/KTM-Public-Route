package com.crestaSom.KTMPublicRoute.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

    public JSONParser() {}

    public JSONObject makeHttpRequest(String urlString, String method) {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod(method);
            conn.connect();
            is = conn.getInputStream();
        } catch (IOException e) {
            Log.e("JSONParser", "Connection error: " + e.toString());
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            return new JSONObject(sb.toString());
        } catch (IOException e) {
            Log.e("JSONParser", "Buffer error: " + e.toString());
        } catch (JSONException e) {
            Log.e("JSONParser", "JSON parse error: " + e.toString());
        }
        return null;
    }
}
