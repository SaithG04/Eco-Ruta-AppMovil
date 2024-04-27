package com.qromarck.reciperu.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.qromarck.reciperu.Entity.Usuario;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class InterfacesUtilities {

    public static SharedPreferences.Editor systemEditor;

    public static SharedPreferences.Editor getSystemEditor(Activity activity) {
        if (systemEditor == null) {
            systemEditor = activity.getSharedPreferences("com.qromarck.reciperu.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit();
        }
        return systemEditor;
    }

    public static <T> Map<String, Object> entityToMap(T entity) {
        Map<String, Object> resultMap = new HashMap<>();
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                resultMap.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace(System.out);
            }
        }
        return resultMap;
    }

    public static String[] obtenerInfoAtributo(Object objeto, Object valorAtributo) {
        Class<?> clazz = objeto.getClass();
        String nombreCampo = null;
        String claseCampo = null;

        try {
            // Iterar sobre los campos de la clase para encontrar el que coincide con el valor del atributo
            for (Field campo : clazz.getDeclaredFields()) {
                campo.setAccessible(true); // Permitir el acceso a campos privados
                Object valor = campo.get(objeto);
                if (valorAtributo.equals(valor)) {
                    nombreCampo = campo.getName();
                    claseCampo = campo.getType().getSimpleName();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return new String[]{claseCampo, nombreCampo};
    }

    public static Object[] mapToArray(Map<String, Object> map) {
        Object[] array = new Object[map.size() * 2];
        int index = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            array[index++] = entry.getKey();
            array[index++] = entry.getValue();
        }
        return array;
    }

    public static <T> Object[] entityToArray(T entity) {
        Map<String, Object> resultMap = entityToMap(entity);
        return mapToArray(resultMap);
    }

    public static void showLoadingIndicator(Activity activity, FrameLayout loadingLayout, ProgressBar loadingIndicator) {
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void hideLoadingIndicator(Activity activity, FrameLayout loadingLayout, ProgressBar loadingIndicator) {
        loadingIndicator.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private static final String PREFERENCES_NAME = "usuario_preferences";
    private static final String KEY_USUARIO = "usuario";

    // Método para guardar un objeto Usuario en SharedPreferences
    public static void guardarUsuario(Context context, Usuario usuario) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (usuario != null) {
            // Convertir el objeto Usuario a JSON
            Gson gson = new Gson();
            String usuarioJson = gson.toJson(usuario);

            // Guardar el JSON en SharedPreferences
            editor.putString(KEY_USUARIO, usuarioJson);
        } else {
            // Si el usuario es null, guardar una cadena vacía en SharedPreferences
            editor.putString(KEY_USUARIO, "");
        }

        editor.apply();
    }


    // Método para recuperar un objeto Usuario de SharedPreferences
    public static Usuario recuperarUsuario(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        // Obtener el JSON del usuario guardado en SharedPreferences
        String usuarioJson = sharedPreferences.getString(KEY_USUARIO, "");

        // Convertir el JSON de vuelta a un objeto Usuario
        Gson gson = new Gson();
        Usuario usuario = gson.fromJson(usuarioJson, Usuario.class);

        return usuario;
    }

    @Deprecated
    public static byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(password.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Deprecated
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    @Deprecated
    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    @Deprecated
    public String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.O)
    public <T> Object[] entityToObjectArray(T entity) {
        if (entity instanceof Usuario) {
            String id = ((Usuario) entity).getId();
            String nombre = ((Usuario) entity).getFull_name();
            String correo = ((Usuario) entity).getEmail();
//            byte[] hashedPassword = ((Usuario) entity).getHashedPassword();
//            byte[] salt = ((Usuario) entity).getSalt();
            String status = ((Usuario) entity).getStatus();

//            String hashedPasswordString = bytesToHex(hashedPassword);
//            String saltString = bytesToHex(salt);

            return new Object[]{
                    id,
                    nombre,
                    correo,
//                    hashedPasswordString,
//                    saltString,
                    status
            };
        }
        return null;
    }
}