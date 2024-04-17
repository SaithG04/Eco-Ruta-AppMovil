package com.qromarck.reciperu.Interfaces;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.qromarck.reciperu.Utilities.CommonServiceUtilities;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
        setContentView(R.layout.activity_registro_ui);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        Button registrarButton = findViewById(R.id.btnREG);
        ImageButton registrarGoogleButton = findViewById(R.id.btnGoogleREG);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        // Use your app or activity context to instantiate a client instance of


        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCampos()) {
                    registrarUsuarioOnFireStore();
                }
            }
        });

        registrarGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuarioOnFireStoreWithGoogleAccount();
            }
        });
    }

    private void registrarUsuarioOnFireStore() {
        showLoadingIndicator();
        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        String password = edtContrasena.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    Date date = new Date();
                    Map<String, Object> map = getStringObjectMap(date, id, fullName, email);

                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl(new Usuario(), RegistroUsuarioUI.this);
                    usuarioDAO.insertOnFireStore(map);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorMessage;
                if (Objects.requireNonNull(e.getMessage()).contains("The email address is already in use by another account.")) {
                    errorMessage = "¡Ups! Parece que alguien más ya está usando ese email.";
                    edtCorreo.setText("");
                    edtCorreo.requestFocus();
                } else if (e.getMessage().contains("The email address is badly formatted.")) {
                    errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
                    edtCorreo.requestFocus();
                } else if (e.getMessage().contains("The given password is invalid. [ Password should be at least 6 characters ]")) {
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

    private void registrarUsuarioOnFireStoreWithGoogleAccount() {
        showLoadingIndicator();


    }

    @NonNull
    private static Map<String, Object> getStringObjectMap(Date date, String id, String fullName, String email) {
        Timestamp timestamp = new Timestamp(date);
        String initialState = "logued out";
        String defaultType = "usuario";

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("full_name", fullName);
        map.put("email", email);
        map.put("registro_date", timestamp);
        map.put("status", initialState);
        map.put("type", defaultType);
        return map;
    }

    private boolean validateCampos() {
        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        String password = edtContrasena.getText().toString();

        if (fullName.isEmpty()) {
            Toast.makeText(RegistroUsuarioUI.this, "Ingrese su nombre completo", Toast.LENGTH_SHORT).show();
            return false;
        } else if (email.isEmpty()) {
            Toast.makeText(RegistroUsuarioUI.this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.isEmpty()) {
            Toast.makeText(RegistroUsuarioUI.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        CommonServiceUtilities.showLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    private void hideLoadingIndicator() {
        CommonServiceUtilities.hideLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }
}
