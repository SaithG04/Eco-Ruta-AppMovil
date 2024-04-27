package com.qromarck.reciperu.Interfaces;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

import java.util.Date;
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
                    registrarUsuarioOnFireStore(nuevoUsuario);
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
        startActivity(new Intent(RegistroUsuarioUI.this, TransitionUI.class));
    }

    private void registrarUsuarioOnFireStore(Usuario usuario) {
        showLoadingIndicator();
        mAuth.createUserWithEmailAndPassword(usuario.getEmail(), edtContrasena.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mAuth.signInWithEmailAndPassword(usuario.getEmail(), edtContrasena.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        //Se le asigna el id generado por firebase al nuevo usuario y se procede a registrar en firestore el resto de sus datos
                                        usuario.setId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                        usuario.setStatus("logged in");

                                        InterfacesUtilities.guardarUsuario(RegistroUsuarioUI.this, usuario);

                                        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, RegistroUsuarioUI.this);
                                        usuarioDAO.insertOnFireStore();
                                        InterfacesUtilities.guardarUsuario(RegistroUsuarioUI.this, usuario);
                                    } else {
                                        System.out.println("NO SE AUTENTICO");
                                    }
                                }
                            });
                }else{
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

    @NonNull
    private Usuario crearUsuario() {

        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        Timestamp registro_date = new Timestamp(new Date());
        String status = "logged out";
        String type = "usuario";

        return new Usuario(fullName, email, registro_date, status, type);
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
