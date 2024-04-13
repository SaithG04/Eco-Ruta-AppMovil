package com.example.reciperu.DAO.DAOImplements;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.DataAccessUtilities;
import com.example.reciperu.Entity.Usuario;

import java.util.HashMap;
import java.util.Map;

public class UsuarioDAOImpl extends DataAccessUtilities implements UsuarioDAO {

    private Usuario usuario;
    private Context context;

    public UsuarioDAOImpl(Usuario usuario, Context context) {
        this.usuario = usuario;
        this.context = context;
    }

    @Override
    public Usuario listar() {
        return null;
    }

    @Override
    public boolean insertar() {
        final boolean[] a = {false};
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL + "insertar_.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                a[0] = true;
                //Toast.makeText(context.getApplicationContext(), "REGISTRO EXITOSO", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                a[0] = false;
                volleyError.printStackTrace(System.out);
                //Toast.makeText(context.getApplicationContext(), "REGISTRO NO EXITOSO", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("nombre", usuario.getNombre());
                parametros.put("correo", usuario.getCorreo());
                parametros.put("hashed_password", usuario.getHashedPassword().toString());
                parametros.put("salt", usuario.getSalt().toString());
                parametros.put("status", usuario.getStatus());
                return parametros;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
        return a[0];
    }

    @Override
    public boolean actualizar() {
        return false;
    }

    @Override
    public boolean eliminar() {
        return false;
    }
}
