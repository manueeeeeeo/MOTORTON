package com.clase.motorton.modelos;

public class Vehiculo {
    private String uidDueno;
    private String matricula;
    private String marca;
    private String modelo;
    private int anos;
    private boolean exportado;
    private String descripción;
    private String tipoVehiculo;

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