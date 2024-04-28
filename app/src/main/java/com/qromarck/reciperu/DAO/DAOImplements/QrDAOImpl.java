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
    private final Activity activity;
    private final static String COLLECTION_NAME = "qrs";

    public QrDAOImpl(QR qr, Activity activity) {
        this.qr = qr;
        this.activity = activity;
    }

    @Override
    public void getQROnFireBase(Object parameter, OnQrRetrievedListener listener) {
        MenuUI menuUI = (MenuUI) activity;
        if (NetworkUtilities.isNetworkAvailable(activity.getApplicationContext())) {

            String[] infoAtributo = obtenerInfoAtributo(qr, parameter);
            String parameterName = infoAtributo[1];

            getByCriteria(QR.class, parameterName, parameter)
                    .addOnSuccessListener(new OnSuccessListener<List<QR>>() {
                        @Override
                        public void onSuccess(List<QR> qr) {
                            // Se obtuvieron los qr correctamente, llamar al método de devolución de llamada
                            if (!qr.isEmpty()) {
                                listener.onQrRetrieved(qr.get(0)); // Pasar el primer usuario encontrado
                            } else {
                                listener.onQrRetrieved(null); // Pasar null si no se encontraron qr
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error al realizar la consulta, llamar al método de devolución de llamada con null
                            listener.onQrRetrieved(null);
                            e.printStackTrace(System.out);
                        }
                    });
        } else {
            menuUI.hideLoadingIndicator();
            DialogUtilities.showNoInternetDialog(activity);
        }
    }

    public interface OnQrRetrievedListener {
        void onQrRetrieved(QR qr);
    }

    @Override
    public void setEntity(QR qr) {
        this.qr = qr;
    }

    @Override
    public void listarFromFireStore(DataAccessUtilities.OnDataRetrievedListener<QR> listener) {

    }

    @Override
    public void insertOnFireStore() {
        Map<String, Object> entityToMap = entityToMap(qr);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, entityToMap,
                new DataAccessUtilities.OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        Toast.makeText(activity.getApplicationContext(), "¡CORRECTO!.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
//                        // Manejar el fallo desde cualquier clase
//                        Toast.makeText(activity.getApplicationContext(), "Error al registrar... ", Toast.LENGTH_SHORT).show();
//                        activity.finish();
//                        Intent intent = new Intent(activity.getApplicationContext(), LoginUI.class);
//                        activity.startActivity(intent);
                        System.out.println("ERROR DE INSERCION: " + errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore() {

    }

    @Override
    public void deleteFromFireStore() {

    }


}
