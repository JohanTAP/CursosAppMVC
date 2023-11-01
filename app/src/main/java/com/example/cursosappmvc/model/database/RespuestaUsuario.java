package com.example.cursosappmvc.model.database;

import java.sql.Timestamp;

public class RespuestaUsuario {
    private int respuestaId;
    private int opcionId;
    private Timestamp fecha;

    // Constructor

    public RespuestaUsuario() {
    }

    // Getters, Setters y Constructor

    public int getRespuestaId() {
        return respuestaId;
    }

    public void setRespuestaId(int respuestaId) {
        this.respuestaId = respuestaId;
    }

    public int getOpcionId() {
        return opcionId;
    }

    public void setOpcionId(int opcionId) {
        this.opcionId = opcionId;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

}