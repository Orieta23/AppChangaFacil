package com.example.chambafacil.ui.slideshow;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Postulacion {

    private String id;
    private String idTrabajo;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String sueldo;
    private Timestamp fecha_postulacion;

    public Postulacion() {
        // Constructor vacío requerido por Firestore
    }

    public Postulacion(String id, String idTrabajo, String titulo, String descripcion,
                       String ubicacion, String sueldo, Timestamp fecha_postulacion) {
        this.id = id;
        this.idTrabajo = idTrabajo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.sueldo = sueldo;
        this.fecha_postulacion = fecha_postulacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTrabajo() { return idTrabajo; }
    public void setIdTrabajo(String idTrabajo) { this.idTrabajo = idTrabajo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getSueldo() { return sueldo; }
    public void setSueldo(String sueldo) { this.sueldo = sueldo; }

    public Timestamp getFecha_postulacion() { return fecha_postulacion; }
    public void setFecha_postulacion(Timestamp fecha_postulacion) { this.fecha_postulacion = fecha_postulacion; }

    public String getFechaFormateada() {
        if (fecha_postulacion == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(fecha_postulacion.toDate());
    }
}