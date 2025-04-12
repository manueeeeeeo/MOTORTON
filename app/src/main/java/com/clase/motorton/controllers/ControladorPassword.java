package com.clase.motorton.controllers;

public class ControladorPassword {

    public static String validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos una letra mayúscula";
        }
        if (!password.matches(".*[a-z].*")) {
            return "La contraseña debe contener al menos una letra minúscula";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos un número";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-\\=\\[\\]{};:'\",.<>?/].*")) {
            return "La contraseña debe contener al menos un carácter especial";
        }
        return "OK";
    }
}