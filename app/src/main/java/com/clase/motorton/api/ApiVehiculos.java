package com.clase.motorton.api;


import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiVehiculos {

    public interface Callback {
        void onResponse(String response);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void obtener(String urlStr, Callback callback) {
        executor.execute(() -> {
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int status = conn.getResponseCode();
                if (status != 200) {
                    postResult(callback, null);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                postResult(callback, response.toString());

            } catch (Exception e) {
                postResult(callback, null);
            }
        });
    }

    private static void postResult(Callback callback, String result) {
        handler.post(() -> callback.onResponse(result));
    }
}