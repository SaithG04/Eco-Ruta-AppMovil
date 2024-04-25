package com.qromarck.reciperu.DAO.DAOImplements;

import static com.qromarck.reciperu.Utilities.CommonServiceUtilities.*;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.*;

import com.google.android.gms.tasks.*;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Interfaces.LoginPrincipalUI;
import com.qromarck.reciperu.Interfaces.MenuUI;
import com.qromarck.reciperu.Utilities.CommonServiceUtilities;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

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
    public void insertOnFireStore() {
        Map<String, Object> entityToMap = entityToMap(usuario);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, entityToMap,
                new OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        Toast.makeText(activity.getApplicationContext(), "¡En hora buena, ahora eres parte de esta familia!.", Toast.LENGTH_LONG).show();
                        activity.finish();
                        Intent intent = new Intent(activity.getApplicationContext(), MenuUI.class);
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
                        // Manejar el fallo desde cualquier clase
                        Toast.makeText(activity.getApplicationContext(), "Error al registrar... ", Toast.LENGTH_SHORT).show();
                        activity.finish();
                        Intent intent = new Intent(activity.getApplicationContext(), LoginPrincipalUI.class);
                        activity.startActivity(intent);
                        System.out.println(errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore() {
        Map<String, Object> entityToMap = entityToMap(usuario);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        updateOnFireStore(COLLECTION_NAME, documentId, entityToMap, new OnUpdateListener() {
            @Override
            public void onUpdateComplete() {

            }

            @Override
            public void onUpdateError(String errorMessage) {
                System.out.println(errorMessage);
            }
        });
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