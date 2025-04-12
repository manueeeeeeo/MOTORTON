package com.clase.motorton.controllers;

import android.util.Patterns;

public class ControladorEmail {

    public static boolean esCorreoValido(String correo) {
        if (correo == null || correo.isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches();
    }
}