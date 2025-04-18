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

    /**
     * @param callback
     * @param urlStr
     * Método para */
    public static void obtener(String urlStr, Callback callback) {
        executor.execute(() -> {
            // Creamos un nuevo stringbuilder para construir la cadena
            StringBuilder response = new StringBuilder();
            // Utilizamos un try catch para poder captar y tratar todas las posibles excepciones
            try {
                // Creamos una nueva url gracias al parametro que le pasamos
                URL url = new URL(urlStr);
                // Creamos una nueva conexión con una url http
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // Establecemos que es de tipo GET
                conn.setRequestMethod("GET");
                // Establecemos el tiempo de conexión
                conn.setConnectTimeout(5000);
                // Establecemos el tiempo de lectura
                conn.setReadTimeout(5000);

                // Obtenemos el estado, gracias al código de respuesta
                int status = conn.getResponseCode();
                // Comprobamos que sea el código de OK
                if (status != 200) { // En caso de que no sea el código de OK
                    // Llamamos al método re
                    postResult(callback, null);
                    // Retornamos
                    return;
                }

                // Creamos un lector para leer todo lo que nos devuelve la conexión
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                // Creamos una variable para ir leyendo líneas
                String line;

                // Mientras que haya líneas por leer y no sea nula
                while ((line = reader.readLine()) != null) {
                    // Agregamos al final de la respuesta la línea
                    response.append(line);
                }

                // Cerramos el lector
                reader.close();
                postResult(callback, response.toString());

            } catch (Exception e) { // En caso de que surja algún error
                postResult(callback, null);
            }
        });
    }

    /**
     * @param callback
     * @param result
     * Método para */
    private static void postResult(Callback callback, String result) {
        handler.post(() -> callback.onResponse(result));
    }
}