package com.example.chambafacil;

public class Postulante {

    private String nombre;
    private String email;
    private String telefono;
    private String ubicacion;
    private String experiencia;

    private String idTrabajo;
    private String idUsuario; // Este será usado como UID del postulante

    public Postulante() {
        // Constructor requerido por Firebase
    }

    public Postulante(String nombre, String email, String telefono,
                      String ubicacion, String experiencia,
                      String idTrabajo, String idUsuario) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.experiencia = experiencia;
        this.idTrabajo = idTrabajo;
        this.idUsuario = idUsuario;
    }

    // ✅ Getter adicional para usar como UID en el adapter
    public String getUid() {
        return idUsuario;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getUbicacion() { return ubicacion; }
    public String getExperiencia() { return experiencia; }
    public String getIdTrabajo() { return idTrabajo; }
    public String getIdUsuario() { return idUsuario; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }
    public void setIdTrabajo(String idTrabajo) { this.idTrabajo = idTrabajo; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
}