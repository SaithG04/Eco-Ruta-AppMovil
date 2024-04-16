package com.qromarck.reciperu.Entity;
import java.util.Date;

public class Usuario {
    private String id;
    private String full_name;
    private String email;
    private byte[] hashedPassword;
    private byte[] salt;
    private Date registro_date;
    private String status;
    private String type;

    // Constructores, getters y setters

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con todos los campos
    public Usuario(String id, String full_name, String email, byte[] hashedPassword, byte[] salt, Date registro_date, String status, String type) {
        this.id = id;
        this.full_name = full_name;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.registro_date = registro_date;
        this.status = status;
        this.type = type;
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

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
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
}