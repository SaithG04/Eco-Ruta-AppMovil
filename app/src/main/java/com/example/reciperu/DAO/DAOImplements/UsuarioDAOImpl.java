package com.example.reciperu.DAO.DAOImplements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Utilities.DataAccessUtilities;

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
        boolean[] succe = {false};
        // Crear la cola de solicitudes de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Si estás mostrando un diálogo, asegúrate de que `context` sea una actividad activa
        if (context instanceof Activity) {
            // Mostrar un diálogo de progreso (opcional) para indicar que se está realizando la operación
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Registrando usuario...");
            progressDialog.show();

            // Llamar a insertarGeneric con el callback
            insertarGeneric(requestQueue, "insertar_usuario.php", usuario, context, new DataAccessUtilities.InsertarGenericCallback() {
                @Override
                public void onInsertarComplete(boolean success) {
                    // Ocultar el diálogo de progreso
                    progressDialog.dismiss();

                    if (success) {
                        // El usuario se registró correctamente
                        Toast.makeText(context, "Usuario registrado con éxito", Toast.LENGTH_LONG).show();
                    } else {
                        // Hubo un error al registrar al usuario
                        Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            // Manejar el caso en el que `context` no sea una actividad activa
            // Por ejemplo, podrías lanzar una excepción o manejar el error de otra forma
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }
        return succe[0];

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
