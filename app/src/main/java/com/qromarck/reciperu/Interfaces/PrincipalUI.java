package com.qromarck.reciperu.Interfaces;


import android.os.Bundle;

import android.view.WindowManager;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import android.content.Intent;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.*;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.security.MessageDigest;

public class PrincipalUI extends AppCompatActivity {

    private Button btnRegistrarse;
    private EditText edtCorreo, edtContrasena;
    private Button btnLogin;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        //Referenciar boton Regisrarse
        btnRegistrarse = findViewById(R.id.btnRegistrar);
        edtCorreo = findViewById(R.id.edtCorreoLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        btnLogin = findViewById(R.id.btnLoginLOG);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        mAuth = FirebaseAuth.getInstance();

        // Configurar el listener del botón
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un Intent para iniciar la nueva actividad
                Intent intent = new Intent(PrincipalUI.this, RegistroUsuarioUI.class);
                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = edtCorreo.getText().toString(); //cambiar id de componente a correo
                String password = edtContrasena.getText().toString();
                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(PrincipalUI.this, "¡Ey, faltan datos!", Toast.LENGTH_SHORT).show();
                } else {
                    loginOnFireBase(correo, password);
                }
            }
        });
    }

    private void loginOnFireBase(String correo, String password) {
        showLoadingIndicator();

        Usuario userSearched = new Usuario();
        userSearched.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched, PrincipalUI.this);
        usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new UsuarioDAOImpl.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(Usuario usuario) {
                if (usuario == null) {
                    hideLoadingIndicator();
                    Toast.makeText(PrincipalUI.this, "No hay ninguna cuenta asociada a este correo.", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                System.out.println(usuario.toString());
                                DataAccessUtilities.usuario = usuario;
                                finish();
                                startActivity(new Intent(PrincipalUI.this, MenuUI.class));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace(System.out);
                            String errorMessage = null;
                            if (e.getMessage().contains("The email address is badly formatted.")) {
                                errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
                                edtCorreo.requestFocus();
                            } else if (e.getMessage().contains("The supplied auth credential is incorrect, malformed or has expired.")) {
                                errorMessage = "Verifique nuevamente su contraseña por favor.";
                                edtContrasena.setText("");
                                edtContrasena.requestFocus();
                            } else if (e.getMessage().contains("We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")) {
                                errorMessage = "Se ha bloqueado temporalmente el acceso a tu cuenta debido a demasiados intentos de inicio de sesión fallidos. Por favor, restablece tu contraseña o inténtalo más tarde.";
                            } else {
                                errorMessage = "Lo sentimos, ha ocurrido un error.";
                            }
                            Toast.makeText(PrincipalUI.this, errorMessage, Toast.LENGTH_LONG).show();
                            hideLoadingIndicator();
                        }
                    });
                }
            }
        });

    }


    // Método para mostrar el indicador de carga y la máscara oscura
    private void showLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.VISIBLE);
        // Además, puedes inhabilitar las interacciones con otros elementos de la interfaz de usuario aquí
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // Método para ocultar el indicador de carga y la máscara oscura
    private void hideLoadingIndicator() {
        loadingIndicator.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
        // Además, puedes habilitar las interacciones con otros elementos de la interfaz de usuario aquí
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}