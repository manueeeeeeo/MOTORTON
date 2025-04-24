package com.clase.motorton.modelos;

public class BusquedaItem {
    private String id;
    private String nombre;
    private String tipo;
    private String imagenUrl;

    public BusquedaItem() {}

    public BusquedaItem(String id, String nombre, String tipo, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.imagenUrl = imagenUrl;
    }

    public BusquedaItem(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getImagenUrl() { return imagenUrl; }
}