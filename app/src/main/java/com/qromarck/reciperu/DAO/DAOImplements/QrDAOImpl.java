package com.qromarck.reciperu.DAO.DAOImplements;

import static com.qromarck.reciperu.Utilities.InterfacesUtilities.entityToMap;
import static com.qromarck.reciperu.Utilities.InterfacesUtilities.obtenerInfoAtributo;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.qromarck.reciperu.DAO.QrDAO;
import com.qromarck.reciperu.Entity.QR;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Interfaces.MenuUI;
import com.qromarck.reciperu.Interfaces.TransitionUI;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QrDAOImpl extends DataAccessUtilities implements QrDAO {
    private QR qr;
    private final static String COLLECTION_NAME = "qrs";

    public QrDAOImpl(QR qr) {
        this.qr = qr;
    }

    @Override
    public void getQROnFireBase(Object parameter, OnSuccessListener<List<QR>> onSuccessListener, OnFailureListener onFailureListener) {

        String[] infoAtributo = obtenerInfoAtributo(qr, parameter);
        String parameterName = infoAtributo[1];

        getByCriteria(QR.class, parameterName, parameter)
                .addOnSuccessListener(new OnSuccessListener<List<QR>>() {
                    @Override
                    public void onSuccess(List<QR> qr) {
                        onSuccessListener.onSuccess(qr);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onFailureListener.onFailure(e);
                    }
                });
    }

    @Override
    public void setEntity(QR qr) {
        this.qr = qr;
    }

    @Override
    public void listarFromFireStore(DataAccessUtilities.OnDataRetrievedListener<QR> listener) {

    }

    @Override
    public void insertOnFireStore(OnInsertionListener listener) {
        Map<String, Object> entityToMap = entityToMap(qr);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, entityToMap,
                new DataAccessUtilities.OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        listener.onInsertionSuccess();
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
                        listener.onInsertionError(errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore(OnUpdateListener listener) {

    }

    @Override
    public void deleteFromFireStore() {

    }


}
