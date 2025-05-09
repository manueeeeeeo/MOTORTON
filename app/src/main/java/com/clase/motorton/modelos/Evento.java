package com.clase.motorton.modelos;

import java.util.ArrayList;
import java.util.Date;

public class Evento {
    private String id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String provincia;
    private String tipoEvento;
    private ArrayList<String> participantes;
    private String organizador;
    private boolean activo;
    private Date fecha;
    private double ubicacionLat;
    private double ubicacionLon;
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private boolean esRuta;

    public Evento() {
    }

    public Evento(String id, String nombre, String descripcion, String ubicacion, String provincia, String tipoEvento, ArrayList<String> participantes, String organizador, boolean activo, Date fecha) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.provincia = provincia;
        this.tipoEvento = tipoEvento;
        this.participantes = participantes;
        this.organizador = organizador;
        this.activo = activo;
        this.fecha = fecha;
    }

    public double getUbicacionLat() {
        return ubicacionLat;
    }

    public void setUbicacionLat(double ubicacionLat) {
        this.ubicacionLat = ubicacionLat;
    }

    public double getUbicacionLon() {
        return ubicacionLon;
    }

    public void setUbicacionLon(double ubicacionLon) {
        this.ubicacionLon = ubicacionLon;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public void setStartLon(double startLon) {
        this.startLon = startLon;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLon() {
        return endLon;
    }

    public void setEndLon(double endLon) {
        this.endLon = endLon;
    }

    public boolean isEsRuta() {
        return esRuta;
    }

    public void setEsRuta(boolean esRuta) {
        this.esRuta = esRuta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public ArrayList<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(ArrayList<String> participantes) {
        this.participantes = participantes;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}