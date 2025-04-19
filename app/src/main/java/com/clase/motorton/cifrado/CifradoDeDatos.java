package com.clase.motorton.cifrado;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class CifradoDeDatos {

    public static String cifrar(String datos) {
        try {
            byte[] datosBytes = datos.getBytes(StandardCharsets.UTF_8);
            return Base64.encodeToString(datosBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String descifrar(String datosCifradosBase64) {
        try {
            byte[] datosDecodificados = Base64.decode(datosCifradosBase64, Base64.DEFAULT);
            return new String(datosDecodificados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}