package com.example.cursosappmvc.model;

public class Leccion {
    private int id;
    private int cursoId;
    private String titulo;
    private String contenido;
    private int ordenLeccion;

    // Constructor

    public Leccion() {
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCursoId() {
        return cursoId;
    }

    public void setCursoId(int cursoId) {
        this.cursoId = cursoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public int getOrdenLeccion() {
        return ordenLeccion;
    }

    public void setOrdenLeccion(int ordenLeccion) {
        this.ordenLeccion = ordenLeccion;
    }

}