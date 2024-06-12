package com.qromarck.reciperu.Interfaces;

import static com.qromarck.reciperu.Utilities.InterfacesUtilities.obtenerInfoAtributo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RegistroUsuarioUI extends AppCompatActivity {

    private EditText edtUsuario, edtCorreo, edtContrasena;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario_ui);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        Button registrarButton = findViewById(R.id.btnREG);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        // Use your app or activity context to instantiate a client instance of


        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCampos()) {
                    Usuario nuevoUsuario = crearUsuario();
                    String password = edtContrasena.getText().toString();
                    registrarUsuarioOnFireStore(nuevoUsuario, password);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingIndicator();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideLoadingIndicator();
        TransitionUI.destino = LoginUI.class;
        Log.d("DEBUG", "FROM: " + RegistroUsuarioUI.class.getSimpleName());
        startActivity(new Intent(RegistroUsuarioUI.this, TransitionUI.class));
        finish();
    }

    private void registrarUsuarioOnFireStore(Usuario usuario, String password) {
        showLoadingIndicator();

        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            mAuth.createUserWithEmailAndPassword(usuario.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mAuth.signInWithEmailAndPassword(usuario.getEmail(), password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            //Se le asigna el id generado por firebase al nuevo usuario y se procede a registrar en firestore el resto de sus datos
                                            usuario.setId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                            usuario.setStatus("logged in");

                                            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario);
                                            System.out.println(usuario.toString());
                                            usuarioDAO.insertOnFireStore(new DataAccessUtilities.OnInsertionListener() {
                                                @Override
                                                public void onInsertionSuccess() {
                                                    usuarioDAO.getUserOnFireBase(usuario.getId(), new OnSuccessListener<List<Usuario>>() {
                                                        @Override
                                                        public void onSuccess(List<Usuario> usuarios) {
                                                            if (!usuarios.isEmpty()) {
                                                                Usuario usuarioGet = usuarios.get(0);
                                                                InterfacesUtilities.guardarUsuario(RegistroUsuarioUI.this, usuarioGet);
                                                                TransitionUI.destino = MenuUI.class;
                                                                Intent intent = new Intent(getApplicationContext(), MenuUI.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                                Toast.makeText(getApplicationContext(), "¡En hora buena, ahora eres parte de esta familia!.", Toast.LENGTH_LONG).show();
                                                                finish();
                                                            } else {
                                                                System.out.println("USUARIO ES NULL");
                                                            }
                                                        }
                                                    }, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace(System.out);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onInsertionError(String errorMessage) {
                                                    System.out.println(errorMessage);
                                                }
                                            });

                                        } else {
                                            System.out.println("NO SE AUTENTICO");
                                        }
                                    }
                                });
                    } else {
                        Log.e("REGISTRO", "NO SE REGISTRO AL USUARIO");
                        hideLoadingIndicator();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMessage;
                    if (Objects.requireNonNull(e.getMessage()).contains("The email address is already in use by another account.")) {
                        errorMessage = "¡Ups! Parece que alguien más ya está usando ese email.";
                        edtCorreo.setError("");
                        edtCorreo.requestFocus();
                    } else if (e.getMessage().contains("The email address is badly formatted.")) {
                        errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
                        edtCorreo.setError("");
                        edtCorreo.requestFocus();
                    } else if (e.getMessage().contains("The given password is invalid. [ Password should be at least 6 characters ]")) {
                        edtContrasena.setError("");
                        errorMessage = "Tu contraseña debe contener al menos 6 caracteres.";
                        edtContrasena.requestFocus();
                    } else if (e.getMessage().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
                        errorMessage = "Parece que estamos desconectados :(";
                    } else {
                        errorMessage = "¡Ups! Algo salió mal.";
                    }
                    hideLoadingIndicator();
                    Toast.makeText(RegistroUsuarioUI.this, errorMessage, Toast.LENGTH_LONG).show();
                    e.printStackTrace(System.out);
                }
            });
        } else {
            hideLoadingIndicator();
            DialogUtilities.showNoInternetDialog(RegistroUsuarioUI.this);
        }
    }

    private boolean validateCampos() {
        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        String password = edtContrasena.getText().toString();

        if (fullName.isEmpty()) {
            edtUsuario.setError("Ingrese su nombre completo");
            return false;
        } else if (email.isEmpty()) {
            edtCorreo.setError("Ingrese su correo");
            return false;
        } else if (password.isEmpty()) {
            edtContrasena.setError("Ingrese una clave");
            return false;
        } else {
            return true;
        }
    }

    //MODIFICADO
    @NonNull
    private Usuario crearUsuario() {
        String fullName = edtUsuario.getText().toString(), email = edtCorreo.getText().toString();
        return new Usuario(fullName, email);
    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    private void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }
}
