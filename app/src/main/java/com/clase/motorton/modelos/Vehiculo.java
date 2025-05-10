package com.clase.motorton.modelos;

import java.io.Serializable;

public class Vehiculo implements Serializable {
    private String uidDueno;
    private String matricula;
    private String marca;
    private String modelo;
    private int anos;
    private boolean exportado;
    private String descripción;
    private String tipoVehiculo;
    private String tuboEscape;
    private String ruedas;
    private String aleron;
    private String dbKiller;
    private boolean bodyKit;
    private boolean lucesLed;
    private double cv;
    private double maxVelocidad;
    private String foto;
    private int choques;

    public Vehiculo() {
    }

    public Vehiculo(String uidDueno, String matricula, String marca, String modelo, int anos, boolean exportado, String descripción, String tipoVehiculo) {
        this.uidDueno = uidDueno;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.anos = anos;
        this.exportado = exportado;
        this.descripción = descripción;
        this.tipoVehiculo = tipoVehiculo;
    }

    public Vehiculo(String uidDueno, String matricula, String marca, String modelo, int anos, boolean exportado,
                    String descripción, String tipoVehiculo, String tubo, String ruedas, String aleron, String dbkiller,
                    boolean bodyKt, boolean lucesL, double maxVel, String foto, int creash, double cv) {
        this.uidDueno = uidDueno;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.anos = anos;
        this.exportado = exportado;
        this.descripción = descripción;
        this.tipoVehiculo = tipoVehiculo;
        this.tuboEscape = tubo;
        this.ruedas = ruedas;
        this.aleron = aleron;
        this.dbKiller = dbkiller;
        this.bodyKit = bodyKt;
        this.lucesLed = lucesL;
        this.maxVelocidad = maxVel;
        this.foto = foto;
        this.choques = creash;
        this.cv = cv;
    }

    public int getChoques() {
        return choques;
    }

    public void setChoques(int choques) {
        this.choques = choques;
    }

    public String getTuboEscape() {
        return tuboEscape;
    }

    public void setTuboEscape(String tuboEscape) {
        this.tuboEscape = tuboEscape;
    }

    public String getRuedas() {
        return ruedas;
    }

    public void setRuedas(String ruedas) {
        this.ruedas = ruedas;
    }

    public String getAleron() {
        return aleron;
    }

    public void setAleron(String aleron) {
        this.aleron = aleron;
    }

    public String getDbKiller() {
        return dbKiller;
    }

    public void setDbKiller(String dbKiller) {
        this.dbKiller = dbKiller;
    }

    public boolean isBodyKit() {
        return bodyKit;
    }

    public void setBodyKit(boolean bodyKit) {
        this.bodyKit = bodyKit;
    }

    public boolean isLucesLed() {
        return lucesLed;
    }

    public void setLucesLed(boolean lucesLed) {
        this.lucesLed = lucesLed;
    }

    public double getCv() {
        return cv;
    }

    public void setCv(double cv) {
        this.cv = cv;
    }

    public double getMaxVelocidad() {
        return maxVelocidad;
    }

    public void setMaxVelocidad(double maxVelocidad) {
        this.maxVelocidad = maxVelocidad;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getUidDueno() {
        return uidDueno;
    }

    public void setUidDueno(String uidDueno) {
        this.uidDueno = uidDueno;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnos() {
        return anos;
    }

    public void setAnos(int anos) {
        this.anos = anos;
    }

    public boolean isExportado() {
        return exportado;
    }

    public void setExportado(boolean exportado) {
        this.exportado = exportado;
    }

    public String getDescripción() {
        return descripción;
    }

    public void setDescripción(String descripción) {
        this.descripción = descripción;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }
}