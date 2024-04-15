package com.example.reciperu.Entity;

public class Usuario {
    private int id;
    private String usuario;
    private String correo;
    private byte[] hashedPassword;
    private byte[] salt;
    private String status;

    public Usuario(int id, String usuario, String correo, byte[] hashedPassword, byte[] salt, String status) {
        this.id = id;
        this.usuario = usuario;
        this.correo = correo;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.status = status;
    }

    public Usuario(String usuario, String correo, byte[] hashedPassword, byte[] salt) {
        id = 0;
        this.usuario = usuario;
        this.correo = correo;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        status = "logued out";
    }

    public Usuario() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
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

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + usuario + '\'' +
                ", correo='" + correo + '\'' +
                ", hashedPassword=" + hashedPassword +
                ", salt=" + salt +
                ", status='" + status + '\'' +
                '}';
    }
}
