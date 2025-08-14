package com.example.chambafacil;

import com.google.firebase.firestore.PropertyName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Trabajo {

    private String id;
    private String titulo;
    private String ubicacion;
    private String sueldo; // ✅ Unificado: ahora usamos "sueldo"
    private Date fecha_publicacion; // ✅ Coincide con Firestore ("fecha_publicacion")
    private String descripcion;

    public Trabajo() {
        // Constructor vacío requerido por Firebase
    }

    public Trabajo(String titulo, String ubicacion, String sueldo, String descripcion, Date fecha_publicacion) {
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.sueldo = sueldo;
        this.descripcion = descripcion;
        this.fecha_publicacion = fecha_publicacion;
    }

    public Trabajo(String id, String titulo, String ubicacion, String sueldo, String descripcion, Date fecha_publicacion) {
        this.id = id;
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.sueldo = sueldo;
        this.descripcion = descripcion;
        this.fecha_publicacion = fecha_publicacion;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getSueldo() { return sueldo; }
    public void setSueldo(String sueldo) { this.sueldo = sueldo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // ✅ Aseguramos el mapeo correcto desde Firestore
    @PropertyName("fecha_publicacion")
    public Date getFechaPublicacion() { return fecha_publicacion; }

    @PropertyName("fecha_publicacion")
    public void setFechaPublicacion(Date fecha_publicacion) { this.fecha_publicacion = fecha_publicacion; }

    // ✅ Fecha formateada
    public String getFechaFormateada() {
        if (fecha_publicacion == null) return "Fecha desconocida";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fecha_publicacion);
    }

    // ✅ Tiempo transcurrido (opcional para mostrar estilo "hace X días")
    public String getTiempoTranscurrido() {
        if (fecha_publicacion == null) return "Publicado recientemente";

        long ahora = System.currentTimeMillis();
        long diferencia = ahora - fecha_publicacion.getTime();

        long segundos = diferencia / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (dias > 0) return "Publicado hace " + dias + (dias == 1 ? " día" : " días");
        if (horas > 0) return "Publicado hace " + horas + (horas == 1 ? " hora" : " horas");
        if (minutos > 0) return "Publicado hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        return "Publicado recién";
    }
}