package com.example.reciperu.Interfaces;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import java.util.HashMap;
import java.util.Map;

public class MySQLService {

    private static final String URL = "https://apireciperu.000webhostapp.com/insertar_.php";

    public static void insertData(Context context, String nombre, String correo, String contrasena) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Toast.makeText(context.getApplicationContext(), "REGISTRO EXITOSO", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace(System.out);
                Toast.makeText(context.getApplicationContext(), "REGISTRO NO EXITOSO", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("nombre", nombre);
                parametros.put("correo", correo);
                parametros.put("contrasena", contrasena);
                return parametros;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}