package com.example.fowltyphoidmonitor.data.api;

import android.os.Handler;
import android.os.Looper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class RequestHandler {

    public interface Callback {
        void onResponse(String response);
    }

    public static void sendPostRequest(String urlString, Map<String, String> params, Callback callback) {
        new Thread(() -> {
            try {
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(entry.getKey()).append('=').append(entry.getValue());
                }

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(postData.toString().getBytes());
                os.flush();
                os.close();

                conn.getResponseCode(); // Just to trigger the request
                new Handler(Looper.getMainLooper()).post(() -> callback.onResponse("Success"));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onResponse("Failed"));
            }
        }).start();
    }
}
