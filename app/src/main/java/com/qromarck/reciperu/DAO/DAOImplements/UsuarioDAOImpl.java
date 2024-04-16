package com.qromarck.reciperu.DAO.DAOImplements;

import static com.qromarck.reciperu.Utilities.CommonServiceUtilities.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.*;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.*;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Interfaces.PrincipalUI;
import com.qromarck.reciperu.Interfaces.RegistroUsuarioUI;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UsuarioDAOImpl extends DataAccessUtilities implements UsuarioDAO {

    private Usuario usuario;
    private final Activity activity;
    private final static String COLLECTION_NAME = "usuarios";

    public interface OnUserRetrievedListener {
        void onUserRetrieved(Usuario usuario);
    }

    public UsuarioDAOImpl(Usuario usuario, Activity activity) {
        this.usuario = usuario;
        this.activity = activity;
    }

    public void setEntity(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void listarFromFireStore(OnDataRetrievedListener<Usuario> listener) {

    }

    @Override
    public void insertOnFireStore(Map<String, Object> userData) {
        String documentId = Objects.requireNonNull(userData.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, userData,
                new OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        Toast.makeText(activity.getApplicationContext(), "¡En hora buena, ahora eres parte de esta familia!.", Toast.LENGTH_LONG).show();
                        activity.finish();
                        Intent intent = new Intent(activity.getApplicationContext(), PrincipalUI.class);
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
                        // Manejar el fallo desde cualquier clase
                        Toast.makeText(activity.getApplicationContext(), "Error al registrar... ", Toast.LENGTH_SHORT).show();
                        System.out.println(errorMessage);
                    }
                });

    }

    @Override
    public void updateOnFireStore() {

    }

    @Override
    public void deleteFromFireStore() {

    }

    @Override
    public void getUserOnFireBase(Object parameter, OnUserRetrievedListener listener) {
        String[] infoAtributo = obtenerInfoAtributo(usuario, parameter);
        String parameterName = infoAtributo[1];

        getByCriteria(Usuario.class, parameterName, parameter)
                .addOnSuccessListener(new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        // Se obtuvieron los usuarios correctamente, llamar al método de devolución de llamada
                        if (!usuarios.isEmpty()) {
                            listener.onUserRetrieved(usuarios.get(0)); // Pasar el primer usuario encontrado
                        } else {
                            listener.onUserRetrieved(null); // Pasar null si no se encontraron usuarios
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error al realizar la consulta, llamar al método de devolución de llamada con null
                        listener.onUserRetrieved(null);
                        e.printStackTrace(System.out);
                    }
                });
    }

}