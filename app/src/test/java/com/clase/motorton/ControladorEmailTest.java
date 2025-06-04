package com.clase.motorton;

import org.junit.Test;

import static org.junit.Assert.*;

import com.clase.motorton.controllers.ControladorEmail;

public class ControladorEmailTest {

    @Test
    public void testCorreoValido() {
        String correo = "usuario@ejemplo.com";
        assertTrue(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoInvalidoSinArroba() {
        String correo = "usuarioejemplo.com";
        assertFalse(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoInvalidoSinDominio() {
        String correo = "usuario@";
        assertFalse(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoInvalidoCadenaVacia() {
        String correo = "";
        assertFalse(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoInvalidoNulo() {
        String correo = null;
        assertFalse(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoValidoConSubdominio() {
        String correo = "usuario@mail.ejemplo.com";
        assertTrue(ControladorEmail.esCorreoValido(correo));
    }

    @Test
    public void testCorreoValidoConNumeros() {
        String correo = "user123@dominio456.com";
        assertTrue(ControladorEmail.esCorreoValido(correo));
    }
}