package com.example.reciperu.Utilities;

import static com.example.reciperu.Utilities.CommonServiceUtilities.*;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.reciperu.Entity.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataAccessUtilities {

    public static final String URL = "https://reciperu2024.000webhostapp.com/";
    private static final int MY_SOCKET_TIMEOUT_MS = 5000; // 5 segundos
    public static String message = "";
    private boolean success = false;

    private Usuario usuario;
    private ArrayList<Usuario> usuarioArrayList = new ArrayList<>();


    public interface OnDataRetrievedListener<T> {
        void onDataRetrieved(ArrayList<T> data);

        void onError(String errorMessage);
    }

    public interface OnInsertionListener {
        void onInsertionSuccess();

        void onInsertionError(String errorMessage);
    }

    public <T> void listarGeneric(RequestQueue requestQueue, String nombreTabla, Class<T> tipoEntidad, OnDataRetrievedListener<T> listener) {
        ArrayList<T> usuarioArrayList = new ArrayList<>(); // Declaración de la lista dentro del método

        try {
            String url = URL + "listar_tabla.php";

            // Crear una solicitud POST
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Imprimir la respuesta recibida antes de intentar analizarla como JSON
                            System.out.println("Response received: " + response);

                            try {
                                JSONArray jsonArray = new JSONArray(response); // response es la cadena que recibes del servidor
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (tipoEntidad == Usuario.class) {
                                        Usuario usuario = new Usuario();
                                        usuario.setId(jsonObject.getInt("id"));
                                        usuario.setNombre(jsonObject.getString("nombre"));
                                        usuario.setCorreo(jsonObject.getString("correo"));
                                        String hashedPasswordHex = jsonObject.getString("hashedPassword");
                                        String salt = jsonObject.getString("salt");
                                        byte[] hashedPasswordBytes = hexStringToByteArray(hashedPasswordHex);
                                        byte[] saltBytes  = hexStringToByteArray(salt);
                                        usuario.setHashedPassword(hashedPasswordBytes);
                                        usuario.setSalt(saltBytes);
                                        usuario.setStatus(jsonObject.getString("status"));
                                        System.out.println(usuario.toString());
                                        usuarioArrayList.add((T) usuario);
                                    }
                                }
                                // Notificar al listener que se han recuperado los datos correctamente
                                listener.onDataRetrieved(usuarioArrayList);
                            } catch (JSONException e) {
                                // Manejar el error de análisis JSON.
                                if (listener != null) {
                                    listener.onError("Error al procesar la respuesta JSON: " + e.getMessage());
                                }
                                e.printStackTrace(System.out);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Manejar el error de la solicitud HTTP
                            if (listener != null) {
                                listener.onError("Error de red: " + error.toString());
                            }
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Agregar parámetros POST si es necesario
                    Map<String, String> params = new HashMap<>();
                    params.put("tableName", nombreTabla);
                    return params;
                }
            };

            // Agregar la solicitud a la cola de solicitudes.
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            // Manejar cualquier otro error
            if (listener != null) {
                listener.onError("Error desconocido: " + e.getMessage());
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public <T> void insertarGeneric(RequestQueue requestQueue, String scriptPhp, T entity, OnInsertionListener listener) {
        try {
            String tableName = determineTableName(scriptPhp);
            obtenerColumnasTabla(requestQueue, tableName, new OnColumnasObtenidasListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onColumnasObtenidas(ArrayList<String> columnas) {
                    try {
                        Map<String, Object> parametros = buildParameterMap(entity, columnas);
                        StringRequest stringRequest = createStringRequest(scriptPhp, parametros, listener);
                        requestQueue.add(stringRequest);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        if (listener != null) {
                            listener.onInsertionError(e.getMessage());
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Error: " + errorMessage);
                    if (listener != null) {
                        listener.onInsertionError(errorMessage);
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace(System.out);
            if (listener != null) {
                listener.onInsertionError(exception.getMessage());
            }
        }
    }

    private StringRequest createStringRequest(String scriptPhp, Map<String, Object> parametros, OnInsertionListener listener) {
        return new StringRequest(Request.Method.POST, URL + scriptPhp,
                response -> {
                    System.out.println("Response: " + response);
                    if (response.equals("Datos insertados correctamente.")) {
                        if (listener != null) {
                            listener.onInsertionSuccess();
                        }
                    } else {
                        if (listener != null) {
                            listener.onInsertionError(response);
                        }
                    }
                },
                error -> {
                    error.printStackTrace(System.out);
                    if (listener != null) {
                        listener.onInsertionError("Error de red: " + error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Aquí necesitas convertir el mapa de parámetros a un mapa de cadenas
                Map<String, String> stringParams = new HashMap<>();
                for (Map.Entry<String, Object> entry : parametros.entrySet()) {
                    stringParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return stringParams;
            }
        };
    }


    private String determineTableName(String scriptPhp) {
        switch (scriptPhp) {
            case "insertar_usuario.php":
                return "usuarios";
            default:
                return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private <T> Map<String, Object> buildParameterMap(T entity, ArrayList<String> columnas) {
        Map<String, Object> parametros = new HashMap<>();
        Object[] objects = new CommonServiceUtilities().entityToObjectArray(entity);
        for (int i = 0; i < columnas.size(); i++) {
            parametros.put(columnas.get(i), objects[i]);

        }
        System.out.println("PARAMETROS: " + parametros);
        return parametros;
    }

    public void obtenerColumnasTabla(RequestQueue requestQueue, String nombreTabla, final OnColumnasObtenidasListener listener) {
        // Define la URL para la solicitud
        try {
            String url = URL + "get_columns.php";
            ArrayList<String> nameColumns = new ArrayList<>();

            // Crea una solicitud POST
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                // Verifica si la respuesta es un JSON válido.
                                JSONArray jsonArray = new JSONArray(response);
                                System.out.println(response);
                                // Itera sobre el array JSON y extrae los nombres de las columnas
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String columnName = jsonArray.getString(i);
                                    nameColumns.add(columnName);
                                }
                                if (listener != null) {
                                    // Llama al método de callback para notificar al cliente que se han obtenido los nombres de las columnas
                                    listener.onColumnasObtenidas(nameColumns);
                                }
                            } catch (JSONException e) {
                                // Maneja el error de análisis JSON.
                                success = false;
                                message = e.getMessage();
                                e.printStackTrace(System.out);
                                if (listener != null) {
                                    listener.onError("Error al procesar la respuesta JSON");

                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace(System.out);
                            if (listener != null) {
                                listener.onError("Error de red: " + error.toString());
                                message = error.toString();
                                success = false;
                            }
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("tableName", nombreTabla);
                    return params;
                }
            };
            // Agrega la solicitud a la cola de solicitudes.
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            success = false;
            message = e.getMessage();
        }
    }


    public interface OnColumnasObtenidasListener {
        void onColumnasObtenidas(ArrayList<String> columnas);

        void onError(String errorMessage);
    }
}