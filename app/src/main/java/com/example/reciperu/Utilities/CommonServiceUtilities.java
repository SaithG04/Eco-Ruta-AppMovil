package com.example.reciperu.Utilities;

import com.example.reciperu.Entity.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class CommonServiceUtilities {

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T>  String[] entityToString(T entity) {
        if (entity instanceof Usuario) {

            int id = ((Usuario) entity).getId();
            String nombre = ((Usuario) entity).getNombre();
            String correo = ((Usuario) entity).getCorreo();
            byte[] hashedPassword = ((Usuario) entity).getHashedPassword();
            byte[] salt = ((Usuario) entity).getSalt();
            String status = ((Usuario) entity).getStatus();

            return new String[]{
                    String.valueOf(id),
                    nombre,
                    correo,
                    Arrays.toString(hashedPassword),
                    Arrays.toString(salt),
                    status
            };
        }
        return null;
    }}
