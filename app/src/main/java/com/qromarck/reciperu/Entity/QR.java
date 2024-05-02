package com.qromarck.reciperu.Entity;

public class QR {
    private String id;
    private int hashed_code;
    private int salt;
    private String sede;
    private int points_value;
    public QR() {
    }

    public QR(String id, int hashed_code, int salt, String sede, int points_value) {
        this.id = id;
        this.hashed_code = hashed_code;
        this.salt = salt;
        this.sede = sede;
        this.points_value = points_value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getHashed_code() {
        return hashed_code;
    }

    public void setHashed_code(int hashed_code) {
        this.hashed_code = hashed_code;
    }

    public int getSalt() {
        return salt;
    }

    public void setSalt(int salt) {
        this.salt = salt;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public int getPoints_value() {
        return points_value;
    }

    public void setPoints_value(int points_value) {
        this.points_value = points_value;
    }
}
