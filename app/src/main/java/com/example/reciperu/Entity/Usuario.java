package com.example.reciperu.Entity;

public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private byte[] hashedPassword;
    private byte[] salt;
    private String status;

    public Usuario(int id, String nombre, String correo, byte[] hashedPassword, byte[] salt, String status) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.status = status;
    }

    public Usuario(String nombre, String correo, byte[] hashedPassword, byte[] salt, String status) {
        this.nombre = nombre;
        this.correo = correo;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.status = status;
    }

    public Usuario() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
