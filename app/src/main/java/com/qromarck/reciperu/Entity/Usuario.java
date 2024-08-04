package com.qromarck.reciperu.Entity;


import com.google.firebase.Timestamp;

public class Usuario {
    private String id;
    private String full_name;
    private String email;
    private Timestamp registro_date;
    private String status;
    private String type;
    private int puntos;
    private Timestamp last_scan_date;
    private String idDevice;

    // Constructores, getters y setters

    // Constructor vacío
    public Usuario(){
    }

    // Constructor con todos los campos
    public Usuario(String full_name, String email, Timestamp registro_date, String status, String type, int puntos, Timestamp last_scan_date) {
        this.full_name = full_name;
        this.email = email;
        this.registro_date = registro_date;
        this.status = status;
        this.type = type;
        this.puntos = puntos;
        this.last_scan_date = last_scan_date;
    }

    public Usuario(String full_name, String email, String idDevice) {
        this.full_name = full_name;
        this.email = email;
        this.type = "usuario";
        this.puntos = 0;
        this.last_scan_date = null;
        this.registro_date = null;
        this.status = "logged out";
        this.idDevice = idDevice;
    }
// Getters y setters para todos los campos

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getRegistro_date() {
        return registro_date;
    }

    public void setRegistro_date(Timestamp registro_date) {
        this.registro_date = registro_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public Timestamp getLast_scan_date() {
        return last_scan_date;
    }

    public void setLast_scan_date(Timestamp last_scan_date) {
        this.last_scan_date = last_scan_date;
    }

    public String getIdDevice() {
        return idDevice;
    }

    public void setIdDevice(String idDevice) {
        this.idDevice = idDevice;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", full_name='" + full_name + '\'' +
                ", email='" + email + '\'' +
                ", registro_date=" + registro_date +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", puntos=" + puntos +
                ", last_scan_date=" + last_scan_date +
                ", idDevice='" + idDevice + '\'' +
                '}';
    }
}