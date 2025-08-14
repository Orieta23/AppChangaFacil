package com.example.chambafacil;

public class Resena {
    private double puntuacion;
    private String comentario;
    private String nombreUsuario; // Nuevo campo agregado

    public Resena() {
        // Constructor vacío requerido por Firestore
    }

    // Constructor con solo puntuación y comentario (compatibilidad previa)
    public Resena(double puntuacion, String comentario) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.nombreUsuario = "Usuario desconocido"; // Valor por defecto
    }

    // Constructor con puntuación, comentario y nombreUsuario
    public Resena(double puntuacion, String comentario, String nombreUsuario) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.nombreUsuario = nombreUsuario;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(double puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}