package com.qromarck.reciperu.Interfaces;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.qromarck.reciperu.Utilities.CommonServiceUtilities;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistroUsuarioUI extends AppCompatActivity {

    private final CommonServiceUtilities csu = new CommonServiceUtilities();

    private EditText edtUsuario, edtCorreo, edtContrasena;
    private Button registrarButton;
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
        registrarButton = findViewById(R.id.btnREG);

        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edtUsuario.getText().toString();
                String email = edtCorreo.getText().toString();
                String password = edtContrasena.getText().toString();

                if (fullName.isEmpty()) {
                    Toast.makeText(RegistroUsuarioUI.this, "Ingrese su nombre completo", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(RegistroUsuarioUI.this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(RegistroUsuarioUI.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                } else {
                    registrarUsuarioOnFireStore(fullName, email, password);
                }
            }
        });
    }

    private void registrarUsuarioOnFireStore(String fullName, String email, String password) {
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
                if (e.getMessage().contains("The email address is already in use by another account.")) {
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

                Toast.makeText(RegistroUsuarioUI.this, errorMessage, Toast.LENGTH_LONG).show();
                e.printStackTrace(System.out);
            }
        });

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
}
