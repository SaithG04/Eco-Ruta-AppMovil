package com.example.reciperu.Utilities;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.reciperu.Entity.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CommonServiceUtilities {

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public <T> Object[] entityToObjectArray(T entity) {
        if (entity instanceof Usuario) {
            int id = ((Usuario) entity).getId();
            String nombre = ((Usuario) entity).getUsuario();
            String correo = ((Usuario) entity).getCorreo();
            byte[] hashedPassword = ((Usuario) entity).getHashedPassword();
            byte[] salt = ((Usuario) entity).getSalt();
            String status = ((Usuario) entity).getStatus();

            String hashedPasswordString = bytesToHex(hashedPassword);
            String saltString = bytesToHex(salt);

            return new Object[]{
                    id,
                    nombre,
                    correo,
                    hashedPasswordString,
                    saltString,
                    status
            };
        }
        return null;
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

}
