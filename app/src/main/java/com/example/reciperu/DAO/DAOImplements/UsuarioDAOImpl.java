package com.example.reciperu.DAO.DAOImplements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

import java.util.ArrayList;

public class UsuarioDAOImpl extends DataAccessUtilities implements UsuarioDAO {

    private Usuario usuario;
    private Context context;

    public UsuarioDAOImpl(Usuario usuario, Context context) {
        this.usuario = usuario;
        this.context = context;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void listar(OnDataRetrievedListener<Usuario> listener) {
        // Crear una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Realizar la solicitud de manera síncrona
        listarGeneric(requestQueue, "usuarios", Usuario.class, new OnDataRetrievedListener<Usuario>() {
            @Override
            public void onDataRetrieved(ArrayList<Usuario> data) {
                // Notificar al listener que se han recuperado los datos
                listener.onDataRetrieved(data);
            }

            @Override
            public void onError(String errorMessage) {
                // Notificar al listener en caso de error
                listener.onError(errorMessage);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void insertar() {
        // Crear la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        insertarGeneric(requestQueue, "insertar_usuario.php", usuario, new OnInsertionListener() {
            @Override
            public void onInsertionSuccess() {
                Toast.makeText(context, "Usuario registrado.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onInsertionError(String errorMessage) {
                Toast.makeText(context, "Usuario no registrado. [" + errorMessage + "]", Toast.LENGTH_LONG).show();
            }
        });
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
