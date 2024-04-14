package com.example.reciperu.Utilities;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


public class DataAccessUtilities {

    public static final String URL = "https://apireciperu.000webhostapp.com/";


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
                String [] nombresC = new String[10];
                int i = 0;
                int longitud = datos.length;
                for (Object dato:datos){
                    parametros.put(nombresC[i], (String) dato);
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


}
