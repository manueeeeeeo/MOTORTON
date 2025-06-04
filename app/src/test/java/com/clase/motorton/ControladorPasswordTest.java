package com.clase.motorton;

import static org.junit.Assert.assertEquals;
import com.clase.motorton.controllers.ControladorPassword;

import org.junit.Test;

public class ControladorPasswordTest {

    @Test
    public void testPasswordVacia() {
        String resultado = ControladorPassword.validarPassword("");
        assertEquals("La contraseña no puede estar vacía", resultado);
    }

    @Test
    public void testPasswordCorta() {
        String resultado = ControladorPassword.validarPassword("A1b!");
        assertEquals("La contraseña debe tener al menos 8 caracteres", resultado);
    }

    @Test
    public void testPasswordSinMayusculas() {
        String resultado = ControladorPassword.validarPassword("abc123!@");
        assertEquals("La contraseña debe contener al menos una letra mayúscula", resultado);
    }

    @Test
    public void testPasswordSinMinusculas() {
        String resultado = ControladorPassword.validarPassword("ABC123!@");
        assertEquals("La contraseña debe contener al menos una letra minúscula", resultado);
    }

    @Test
    public void testPasswordSinNumero() {
        String resultado = ControladorPassword.validarPassword("Abcdef!@");
        assertEquals("La contraseña debe contener al menos un número", resultado);
    }

    @Test
    public void testPasswordSinCaracterEspecial() {
        String resultado = ControladorPassword.validarPassword("Abcdef12");
        assertEquals("La contraseña debe contener al menos un carácter especial", resultado);
    }

    @Test
    public void testPasswordCorrecta() {
        String resultado = ControladorPassword.validarPassword("Abcdef1!");
        assertEquals("OK", resultado);
    }
}