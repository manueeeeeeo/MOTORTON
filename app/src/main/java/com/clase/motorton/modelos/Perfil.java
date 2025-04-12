package com.clase.motorton.modelos;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Perfil {
    private String uid;
    private String username;
    private String email;
    private String nombre_completo;
    private String ubicacion;
    private int edad;
    private String fechaNaci;
    private int cp;
    private ArrayList<Vehiculo> listaVehiculos;
    private Bitmap fotoPerfil;

    public Perfil() {
    }

    public Perfil(String uid, String username, String nombre_completo, String ubicacion, int edad, String fechaNaci, int cp, ArrayList<Vehiculo> listaVehiculos, Bitmap fotoPerfil, String email) {
        this.uid = uid;
        this.username = username;
        this.nombre_completo = nombre_completo;
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.fechaNaci = fechaNaci;
        this.cp = cp;
        this.listaVehiculos = listaVehiculos;
        this.fotoPerfil = fotoPerfil;
        this.email = email;
    }

    public Perfil(String uid, String username, String nombre_completo, String ubicacion, int edad, String fechaNaci, int cp, Bitmap fotoPerfil, String email) {
        this.uid = uid;
        this.username = username;
        this.nombre_completo = nombre_completo;
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.fechaNaci = fechaNaci;
        this.cp = cp;
        this.listaVehiculos = new ArrayList<>();
        this.fotoPerfil = fotoPerfil;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getFechaNaci() {
        return fechaNaci;
    }

    public void setFechaNaci(String fechaNaci) {
        this.fechaNaci = fechaNaci;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public ArrayList<Vehiculo> getListaVehiculos() {
        return listaVehiculos;
    }

    public void setListaVehiculos(ArrayList<Vehiculo> listaVehiculos) {
        this.listaVehiculos = listaVehiculos;
    }

    public Bitmap getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(Bitmap fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}