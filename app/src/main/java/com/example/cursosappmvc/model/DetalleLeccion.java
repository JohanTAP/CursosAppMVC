package com.example.cursosappmvc.model;

import java.sql.Date;

public class DetalleLeccion {
    private int id;
    private int usuarioId;
    private int leccionId;
    private Date fechaInicio;
    private String estadoProgreso;

    // Constructor

    public DetalleLeccion() {
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getLeccionId() {
        return leccionId;
    }

    public void setLeccionId(int leccionId) {
        this.leccionId = leccionId;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getEstadoProgreso() {
        return estadoProgreso;
    }

    public void setEstadoProgreso(String estadoProgreso) {
        this.estadoProgreso = estadoProgreso;
    }

}