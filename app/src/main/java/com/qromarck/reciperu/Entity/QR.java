package com.qromarck.reciperu.Entity;

public class QR {
    private String id;
    private int hashed_code;
    private int salt;

    public QR() {
    }

    public QR(String id, int hashed_code, int salt) {
        this.id = id;
        this.hashed_code = hashed_code;
        this.salt = salt;
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
}
