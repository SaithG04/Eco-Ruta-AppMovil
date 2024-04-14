package com.example.reciperu.Utilities;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.reciperu.Entity.Usuario;

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
        obtenerColumnasTabla(nameTable, context, new DataAccessUtilities.OnColumnasObtenidasListener() {
            @Override
            public void onColumnasObtenidas(ArrayList<Column> columnas) {
                // Aquí puedes manejar las columnas obtenidas
//                for (Column columna : columnas) {

//                }
            }

            @Override
            public void onError(String errorMessage) {
                // Manejar el error en caso de que ocurra
                System.out.println("Error: " + errorMessage);
            }
        });
        return null;
    }
    public boolean insertarGeneric(String script_php, Object [] datos, Context context) {
        boolean pene = false;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + script_php, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                volleyError.printStackTrace(System.out);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                String tableName = "";
                switch (script_php){
                    case "insetar_.php":
                        tableName = "users";
                        break;
                    case "":
                        break;
                }
                ArrayList<String> columnasTabla = obtenerColumnasTabla(tableName, context, new OnColumnasObtenidasListener() {
                    @Override
                    public void onColumnasObtenidas(ArrayList<Column> columnas) {
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Manejar el error en caso de que ocurra
                        System.out.println("Error: " + errorMessage);
                    }
                });

                for (int i = 0; i < columnasTabla.size(); i++){
                    parametros.put(columnasTabla.get(i), (String) datos[i]);
                }

                /*parametros.put("nombre", usuario.getNombre());
                parametros.put("correo", usuario.getCorreo());
                parametros.put("hashed_password", usuario.getHashedPassword().toString());
                parametros.put("salt", usuario.getSalt().toString());
                parametros.put("status", usuario.getStatus());*/
                return parametros;
            }

        };
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
            pene = true;
        }catch(Exception err){
            err.printStackTrace(System.out);
        }
        return pene;

    }

    public ArrayList<String> obtenerColumnasTabla(String nombreTabla, Context context, final OnColumnasObtenidasListener listener) {
        ArrayList<String> nameColumns = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + "GetNameTables.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    ArrayList<Column> columnas = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String nombre = jsonObject.getString("Field");
                        String tipo = jsonObject.getString("Type");
                        String nullable = jsonObject.getString("Null");
                        columnas.add(new Column(nombre, tipo, nullable));
                        nameColumns.add(nombre);
                    }
                    if (listener != null) {
                        listener.onColumnasObtenidas(columnas);
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
        return nameColumns;
    }

    public interface OnColumnasObtenidasListener {
        void onColumnasObtenidas(ArrayList<Column> columnas);
        void onError(String errorMessage);
    }

    // Definición de la clase Column
    public static class Column {
        public String name;
        public String type;
        public String nullable;

        public Column(String name, String type, String nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }
    }
}
