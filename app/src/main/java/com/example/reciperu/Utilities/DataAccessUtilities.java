package com.example.reciperu.Utilities;
import android.content.Context;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DataAccessUtilities {

    public static final String URL = "https://apireciperu.000webhostapp.com/";

    public <T> T listarGeneric(Context context, String nameTable, T entity){
        // Aquí deberías implementar la lógica para obtener los datos de la tabla
        // Puedes utilizar el método obtenerColumnasTabla() como referencia
        return null;
    }

    public boolean insertarGeneric(RequestQueue requestQueue, String script_php, Object[] datos, Context context) {
        // Asigna el nombre de la tabla según el script
        String tableName = "";
        switch (script_php){
            case "insetar_.php":
                tableName = "users";
                break;
            case "":
                break;
        }

        // Llama al método para obtener las columnas
        obtenerColumnasTabla(requestQueue, tableName, context, new OnColumnasObtenidasListener() {
            @Override
            public void onColumnasObtenidas(ArrayList<String> columnas) {
                // Aquí puedes construir el mapa de parámetros para la solicitud
                Map<String, String> parametros = new HashMap<>();
                for (int i = 0; i < columnas.size(); i++){
                    parametros.put(columnas.get(i), (String) datos[i]);
                }

                // Crea la solicitud para insertar los datos en la tabla
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + script_php,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                // Manejar la respuesta si es necesario
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace(System.out);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        return parametros;
                    }
                };

                // Agrega la solicitud a la cola de solicitudes
                requestQueue.add(stringRequest);
            }

            @Override
            public void onError(String errorMessage) {
                // Manejar el error en caso de que ocurra
                System.out.println("Error: " + errorMessage);
            }
        });

        return false;
    }


    public void obtenerColumnasTabla(RequestQueue requestQueue, String nombreTabla, Context context, final OnColumnasObtenidasListener listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + "GetNameTables.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            ArrayList<String> nameColumns = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String nombre = jsonObject.getString("Field");
                                nameColumns.add(nombre);
                            }
                            if (listener != null) {
                                listener.onColumnasObtenidas(nameColumns);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (listener != null) {
                                listener.onError("Error al procesar la respuesta JSON");
                            }
                        }
                    }
                }, new Response.ErrorListener() {
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

        requestQueue.add(stringRequest);
    }

    public interface OnColumnasObtenidasListener {
        void onColumnasObtenidas(ArrayList<String> columnas);
        void onError(String errorMessage);
    }
}