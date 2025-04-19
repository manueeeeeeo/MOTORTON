package com.clase.motorton.controllers;

import android.util.Patterns;

public class ControladorEmail {

    /**
     * @return
     * @param correo
     * Método en el que le paso como parametro
     * una cadena que representa el correo y devuelvo
     * un true o false comprobando si está vacío
     * o si cumple con el formato de correo
     */
    public static boolean esCorreoValido(String correo) {
        // Comprobamos si la cadena es nula o está vacía
        if (correo == null || correo.isEmpty()) {
            // Retornamos false en caso afirmativo
            return false;
        }
        // Retornamos la comprobación del correo
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches();
    }
}