package com.example.reciperu.DAO.DAOImplements;

import static com.example.reciperu.Utilities.CommonServiceUtilities.*;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.CommonServiceUtilities;
import com.example.reciperu.Utilities.DataAccessUtilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

public class UsuarioDAOImpl extends DataAccessUtilities implements UsuarioDAO {

    private Usuario usuario;
    private final Context context;
    private final static String TABLE_NAME = "usuarios";

    public UsuarioDAOImpl(Usuario usuario, Context context) {
        this.usuario = usuario;
        this.context = context;
    }

    public void setEntity(Usuario usuario) {
        this.usuario = usuario;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void listar(OnDataRetrievedListener<Usuario> listener) {
        // Crear una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Realizar la solicitud de manera síncrona
        listarGeneric(requestQueue, TABLE_NAME, new OnDataRetrievedListener<Usuario>() {
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
        insertarGeneric(requestQueue, TABLE_NAME, usuario, new OnInsertionListener() {
            @Override
            public void onInsertionSuccess() {
                Toast.makeText(context, "Usuario registrado.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onInsertionError(String errorMessage) {
                Toast.makeText(context, "Error. Usuario no registrado.", Toast.LENGTH_LONG).show();
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

    @Override
    public void getUserBy(Object parameter, OnDataRetrievedOneListener<Usuario> listener) {
        // Crear una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Llamar al método para obtener la clase, y el nombre del atributo
        String[] infoAtributo = obtenerInfoAtributo(usuario, parameter);

        // Asignar la clase y el nombre del atributo
        String parameterType = infoAtributo[0];
        String parameterName = infoAtributo[1];

        // Realizar la solicitud de manera síncrona
        getEntityByParameter(requestQueue, TABLE_NAME, parameterName, parameter, parameterType, new OnDataRetrievedOneListener<Usuario>() {
            @Override
            public void onDataRetrieved(Usuario usuario) {
                // Notificar al listener que se han recuperado los datos
                listener.onDataRetrieved(usuario);
            }

            @Override
            public void onError(String errorMessage) {
                // Notificar al listener en caso de error
                listener.onError(errorMessage);
            }
        });
    }
}