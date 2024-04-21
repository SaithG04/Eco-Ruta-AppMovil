package com.qromarck.reciperu.Entity;
import java.util.Date;

public class Usuario {
    private String id;
    private String full_name;
    private String email;
    private Date registro_date;
    private String status;
    private String type;
    private double last_latitude;
    private double last_longitude;
    private Date last_update_ubication_date;

    // Constructores, getters y setters

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con todos los campos

    public Usuario(String id, String full_name, String email, Date registro_date, String status, String type, double last_latitude, double last_longitude, Date last_update_ubication_date) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.registro_date = registro_date;
        this.status = status;
        this.type = type;
        this.last_latitude = last_latitude;
        this.last_longitude = last_longitude;
        this.last_update_ubication_date = last_update_ubication_date;
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

    public Date getRegistro_date() {
        return registro_date;
    }

    public void setRegistro_date(Date registro_date) {
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

    public double getLast_latitude() {
        return last_latitude;
    }

    public void setLast_latitude(double last_latitude) {
        this.last_latitude = last_latitude;
    }

    public double getLast_longitude() {
        return last_longitude;
    }

    public void setLast_longitude(double last_longitude) {
        this.last_longitude = last_longitude;
    }

    public Date getLast_update_ubication_date() {
        return last_update_ubication_date;
    }

    public void setLast_update_ubication_date(Date last_update_ubication_date) {
        this.last_update_ubication_date = last_update_ubication_date;
    }
}