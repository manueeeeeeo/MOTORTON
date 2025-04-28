package com.clase.motorton.modelos;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Perfil {
    private String uid;
    private String username;
    private String email;
    private String nombre_completo;
    private Map<String, Object> ubicacion;
    private int edad;
    private String fechaNaci;
    private int cp;
    private ArrayList<Vehiculo> listaVehiculos;
    private Bitmap fotoPerfil;
    private String descripcion;
    private int aniosConduciendo;
    private ArrayList<String> usuariosLikeYou;

    public Perfil() {
    }

    public Perfil(String uid, String username, String email, String nombre_completo, Map<String, Object> ubicacion, int edad, String fechaNaci, int cp, ArrayList<Vehiculo> listaVehiculos, Bitmap fotoPerfil, String descripcion, int aniosConduciendo) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.nombre_completo = nombre_completo;
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.fechaNaci = fechaNaci;
        this.cp = cp;
        this.listaVehiculos = listaVehiculos;
        this.fotoPerfil = fotoPerfil;
        this.descripcion = descripcion;
        this.aniosConduciendo = aniosConduciendo;
    }

    public Perfil(String uid, String username, String email, String nombre_completo, Map<String, Object> ubicacion, int edad, String fechaNaci, int cp, ArrayList<Vehiculo> listaVehiculos, Bitmap fotoPerfil, String descripcion, int aniosConduciendo, ArrayList<String> likes) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.nombre_completo = nombre_completo;
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.fechaNaci = fechaNaci;
        this.cp = cp;
        this.listaVehiculos = listaVehiculos;
        this.fotoPerfil = fotoPerfil;
        this.descripcion = descripcion;
        this.aniosConduciendo = aniosConduciendo;
        this.usuariosLikeYou = likes;
    }

    public ArrayList<String> getUsuariosLikeYou() {
        return usuariosLikeYou;
    }

    public void setUsuariosLikeYou(ArrayList<String> usuariosLikeYou) {
        this.usuariosLikeYou = usuariosLikeYou;
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

    public Map<String, Object> getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Map<String, Object> ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getAniosConduciendo() {
        return aniosConduciendo;
    }

    public void setAniosConduciendo(int aniosConduciendo) {
        this.aniosConduciendo = aniosConduciendo;
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