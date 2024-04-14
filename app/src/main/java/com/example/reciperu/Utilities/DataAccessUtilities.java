package com.example.reciperu.Utilities;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class DataAccessUtilities {

    public static final String URL = "https://apireciperu.000webhostapp.com/";
    private boolean successProcess = false;

    public interface InsertarGenericCallback {
        void onInsertarComplete(boolean success);
    }

    public <T> T listarGeneric(Context context, String nameTable, T entity) {
        // Aquí deberías implementar la lógica para obtener los datos de la tabla
        // Puedes utilizar el método obtenerColumnasTabla() como referencia
        return null;
    }
    public <T> void insertarGeneric(RequestQueue requestQueue, String scriptPhp, T entity, Context context, InsertarGenericCallback callback) {
        // Iniciar la operación
        String tableName = determineTableName(scriptPhp);

        obtenerColumnasTabla(requestQueue, tableName, context, new OnColumnasObtenidasListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onColumnasObtenidas(ArrayList<String> columnas) {
                try {
                    Map<String, String> parametros = buildParameterMap(entity, columnas);
                    StringRequest stringRequest = createStringRequest(scriptPhp, parametros);
                    requestQueue.add(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    callback.onInsertarComplete(false);
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("Error: " + errorMessage);
                callback.onInsertarComplete(false);
            }
        });
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
    private <T> Map<String, String> buildParameterMap(T entity, ArrayList<String> columnas) {
        Map<String, String> parametros = new HashMap<>();
        String[] strings = new CommonServiceUtilities().entityToString(entity);
        for (int i = 0; i < columnas.size(); i++) {
            parametros.put(columnas.get(i), strings[i]);
        }
        return parametros;
    }

    private StringRequest createStringRequest(String scriptPhp, Map<String, String> parametros) {
        return new StringRequest(Request.Method.POST, URL + scriptPhp,
                response -> {
                    System.out.println("Response: " + response);
                    successProcess = true; // La respuesta fue exitosa
                },
                error -> {
                    error.printStackTrace(System.out);
                    successProcess = false; // Hubo un error en la solicitud
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parametros;
            }
        };
    }


    public void obtenerColumnasTabla(RequestQueue requestQueue, String nombreTabla, Context context, final OnColumnasObtenidasListener listener) {
        // Define la URL para la solicitud
        String url = URL + "GetNameTables.php";

        // Crea una solicitud POST
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response: " + response);
                        try {
                            // Verifica si la respuesta es un JSON válido.
                            JSONArray jsonArray = new JSONArray(response);

                            ArrayList<String> nameColumns = new ArrayList<>();

                            // Itera sobre el array JSON y extrae los nombres de las columnas
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String columnName = jsonArray.getString(i);
                                nameColumns.add(columnName);
                            }

                            // Llama al listener para notificar los nombres de las columnas
                            if (listener != null) {
                                listener.onColumnasObtenidas(nameColumns);
                            }
                        } catch (JSONException e) {
                            // Maneja el error de análisis JSON.
                            e.printStackTrace();
                            if (listener != null) {
                                listener.onError("Error al procesar la respuesta JSON");
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (listener != null) {
                            listener.onError("Error de red: " + error.toString());
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
    }



    public interface OnColumnasObtenidasListener {
        void onColumnasObtenidas(ArrayList<String> columnas);

        void onError(String errorMessage);
    }
}