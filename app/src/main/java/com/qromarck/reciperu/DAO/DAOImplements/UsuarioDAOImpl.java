package com.qromarck.reciperu.DAO.DAOImplements;

import static com.qromarck.reciperu.Utilities.InterfacesUtilities.*;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.*;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Interfaces.LoginUI;
import com.qromarck.reciperu.Interfaces.MenuUI;
import com.qromarck.reciperu.Interfaces.ReciShop;
import com.qromarck.reciperu.Interfaces.TransitionUI;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

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
    public void insertOnFireStore(OnInsertionListener insertionListener) {
        Map<String, Object> entityToMap = entityToMap(usuario);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, entityToMap,
                new OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        insertionListener.onInsertionSuccess();
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
                        insertionListener.onInsertionError(errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore(OnUpdateListener onUpdateListener) {

        Map<String, Object> entityToMap = entityToMap(usuario);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        updateOnFireStore(COLLECTION_NAME, documentId, entityToMap, new OnUpdateListener() {
            @Override
            public void onUpdateComplete() {
                onUpdateListener.onUpdateComplete();
            }

            @Override
            public void onUpdateError(String errorMessage) {
                onUpdateListener.onUpdateError(errorMessage);
            }
        });

    }

    @Override
    public void deleteFromFireStore() {

    }

    @Override
    public void getUserOnFireBase(Object parameter, OnSuccessListener<List<Usuario>> onSuccessListener, OnFailureListener onFailureListener) {

        String[] infoAtributo = obtenerInfoAtributo(usuario, parameter);
        String parameterName = infoAtributo[1];

        getByCriteria(Usuario.class, parameterName, parameter)
                .addOnSuccessListener(new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        onSuccessListener.onSuccess(usuarios);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onFailureListener.onFailure(e);
                    }
                });
    }

}