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
    public void insertOnFireStore() {
        Map<String, Object> entityToMap = entityToMap(usuario);
        String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
        insertOnFireStore(COLLECTION_NAME, documentId, entityToMap,
                new OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {

                        TransitionUI.destino = MenuUI.class;
                        Intent intent = new Intent(activity.getApplicationContext(), MenuUI.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                        Toast.makeText(activity.getApplicationContext(), "¡En hora buena, ahora eres parte de esta familia!.", Toast.LENGTH_LONG).show();
                        activity.finish();
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
//                        // Manejar el fallo desde cualquier clase
//                        Toast.makeText(activity.getApplicationContext(), "Error al registrar... ", Toast.LENGTH_SHORT).show();
//                        activity.finish();
//                        Intent intent = new Intent(activity.getApplicationContext(), LoginUI.class);
//                        activity.startActivity(intent);
                        System.out.println(errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore() {

        boolean deslogueo = MenuUI.typeChange.equals("deslogueo");
        boolean sumaptos = MenuUI.typeChange.equals("sumaptos");
        boolean restaptos = ReciShop.typeChange.equals("restaptos");
        MenuUI menuUIActivity = (deslogueo || sumaptos) ? (MenuUI) activity : null;
        ReciShop reciShop = restaptos ? (ReciShop) activity : null;

        if (NetworkUtilities.isNetworkAvailable(activity.getApplicationContext())) {
            Map<String, Object> entityToMap = entityToMap(usuario);
            String documentId = Objects.requireNonNull(entityToMap.get("id")).toString();
            updateOnFireStore(COLLECTION_NAME, documentId, entityToMap, new OnUpdateListener() {
                @Override
                public void onUpdateComplete() {
                    if (menuUIActivity != null) {
                        if (MenuUI.typeChange.equals("sumaptos")) {
                            InterfacesUtilities.guardarUsuario(menuUIActivity.getApplicationContext(),usuario);
                            menuUIActivity.getReci().setText(String.valueOf(usuario.getPuntos()));
                            menuUIActivity.hideLoadingIndicator();
                            Toast.makeText(menuUIActivity.getApplicationContext(),"Puntos Agregados Correctamente",Toast.LENGTH_SHORT).show();
                        }else{
                            FirebaseAuth.getInstance().signOut();
                            InterfacesUtilities.guardarUsuario(menuUIActivity.getApplicationContext(), null);

                            TransitionUI.destino = LoginUI.class;
                            Log.d("DEBUG", "FROM: " + UsuarioDAOImpl.class.getSimpleName());
                            menuUIActivity.startActivity(new Intent(menuUIActivity.getApplicationContext(), TransitionUI.class));
                            // Finaliza la actividad actual
                            menuUIActivity.finish();
                        }

                    } else if (reciShop !=null) {
                        if(ReciShop.typeChange.equals("restaptos")){
                            InterfacesUtilities.guardarUsuario(reciShop.getApplicationContext(),usuario);
                            reciShop.getPtos().setText(String.valueOf(usuario.getPuntos()));
                            Toast.makeText(reciShop.getApplicationContext(),"Recompensa Canjeada!!!",Toast.LENGTH_SHORT).show();
                        }else{
                            FirebaseAuth.getInstance().signOut();
                            InterfacesUtilities.guardarUsuario(reciShop.getApplicationContext(), null);

                            TransitionUI.destino = LoginUI.class;
                            Log.d("DEBUG", "FROM: " + UsuarioDAOImpl.class.getSimpleName());
                            reciShop.startActivity(new Intent(reciShop.getApplicationContext(), TransitionUI.class));
                            // Finaliza la actividad actual
                            reciShop.finish();
                        }
                    }
                }

                @Override
                public void onUpdateError(String errorMessage) {
                    if (menuUIActivity != null) {
                            Toast.makeText(menuUIActivity.getApplicationContext(), MenuUI.typeChange.equals("sumaptos") ? "Error al agregar puntos." : "Error al cerrar sesión.", Toast.LENGTH_SHORT).show();
                            menuUIActivity.hideLoadingIndicator();
                    }else if(reciShop != null) {
                        Toast.makeText(reciShop.getApplicationContext(), MenuUI.typeChange.equals("restaptos") ? "Error al canjear recompensa." : "Error al cerrar sesión.", Toast.LENGTH_SHORT).show();
                        // reciShop.hideLoadingIndicator();
                    }
                    System.out.println(errorMessage);
                }
            });
        } else {
            if (menuUIActivity != null) {
                menuUIActivity.hideLoadingIndicator();
            }
            DialogUtilities.showNoInternetDialog(activity);
        }
    }

    @Override
    public void deleteFromFireStore() {

    }

    @Override
    public void getUserOnFireBase(Object parameter, OnUserRetrievedListener listener) {

        boolean login = LoginUI.type.equals("login");
        LoginUI loginUIActivity = login ? (LoginUI) activity : null;

        if (NetworkUtilities.isNetworkAvailable(activity.getApplicationContext())) {

            String[] infoAtributo = obtenerInfoAtributo(usuario, parameter);
            String parameterName = infoAtributo[1];

            getByCriteria(Usuario.class, parameterName, parameter)
                    .addOnSuccessListener(new OnSuccessListener<List<Usuario>>() {
                        @Override
                        public void onSuccess(List<Usuario> usuarios) {
                            // Se obtuvieron los usuarios correctamente, llamar al método de devolución de llamada
                            if (!usuarios.isEmpty()) {
                                System.out.println(usuarios.get(0).toString());
                                listener.onUserRetrieved(usuarios.get(0)); // Pasar el primer usuario encontrado
                            } else {
                                System.out.println("USUARIO ES NULL");
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
        } else {
            if (loginUIActivity != null) {
                loginUIActivity.hideLoadingIndicator();
            }
            DialogUtilities.showNoInternetDialog(activity);
        }

    }

}