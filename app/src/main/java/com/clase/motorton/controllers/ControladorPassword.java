package com.clase.motorton.controllers;

public class ControladorPassword {

    /**
     * @return
     * @param password
     * Método en el que le paso como parametro una cadena
     * que representa la clave del usuario y comprobamos
     * que cumpla todas las caracteristicas de seguridad que
     * tienen dichas claves y retornamos el primer fallo encontrado
     */
    public static String validarPassword(String password) {
        if (password == null || password.isEmpty()) { // Comprobamos que no esté vacia
            return "La contraseña no puede estar vacía";
        }
        if (password.length() < 8) { // Comprobamos si es demasiado corta
            return "La contraseña debe tener al menos 8 caracteres";
        }
        if (!password.matches(".*[A-Z].*")) { // Comprobamos si no contiene alguna mayuscula
            return "La contraseña debe contener al menos una letra mayúscula";
        }
        if (!password.matches(".*[a-z].*")) { // Comprobamos si no contiene alguna minuscula
            return "La contraseña debe contener al menos una letra minúscula";
        }
        if (!password.matches(".*\\d.*")) { // Comprobamos si no contiene números
            return "La contraseña debe contener al menos un número";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-\\=\\[\\]{};:'\",.<>?/].*")) { // Comprobamos si no contiene algún caracter especiañ
            return "La contraseña debe contener al menos un carácter especial";
        }
        // En caso de superar todos los filtros retornamos OK
        return "OK";
    }
}