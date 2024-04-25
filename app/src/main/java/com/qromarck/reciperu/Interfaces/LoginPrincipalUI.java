package com.qromarck.reciperu.Interfaces;

import android.os.Bundle;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import android.content.Intent;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.util.Objects;

/**
 * Clase que representa la interfaz de usuario principal de la aplicación.
 */
public class LoginPrincipalUI extends AppCompatActivity {

    // Declaración de variables

    /**
     * Campo de texto para el correo electrónico.
     */
    private EditText edtCorreo;
    /**
     * Campo de texto para la contraseña.
     */
    private EditText edtContrasena;
    /**
     * Diseño de la interfaz de usuario para mostrar el indicador de carga.
     */
    private FrameLayout loadingLayout;
    /**
     * Indicador de carga.
     */
    private ProgressBar loadingIndicator;
    /**
     * Instancia de FirebaseAuth para la autenticación de Firebase.
     */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el diseño de la actividad y configurar bordes transparentes
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar elementos de la interfaz de usuario
        Button btnRegistrarse = findViewById(R.id.btnRegistrar);
        Button btnLogin = findViewById(R.id.btnLoginLOG);
        ImageView viewRegGoogle = findViewById(R.id.viewRegistrarGoogle);
        edtCorreo = findViewById(R.id.edtCorreoLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Inicializar FirebaseAuth para la autenticación de Firebase
        mAuth = FirebaseAuth.getInstance();

        Usuario usuario = CommonServiceUtilities.recuperarUsuario(getApplicationContext());
        if (usuario != null) {
            // El usuario está logueado
            // Puedes iniciar la actividad que corresponda, por ejemplo:
            startActivity(new Intent(this, MenuUI.class));
            finish();
        }

        // Configurar el botón para abrir la actividad de registro de usuario
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPrincipalUI.this, RegistroUsuarioUI.class);
                startActivity(intent);
                finish();
            }
        });

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = edtCorreo.getText().toString();
                String password = edtContrasena.getText().toString();
                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginPrincipalUI.this, "¡Ey, faltan datos!", Toast.LENGTH_SHORT).show();
                } else {
                    loginOnFireBase(correo, password); // Método para iniciar sesión en Firebase
                }
            }
        });
        viewRegGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * Método para iniciar sesión en Firebase.
     *
     * @param correo   Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    private void loginOnFireBase(String correo, String password) {
        showLoadingIndicator(); // Mostrar indicador de carga

        // Crear un usuario con el correo proporcionado
        Usuario userSearched = new Usuario();
        userSearched.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched, LoginPrincipalUI.this);

        // Buscar usuario en Firebase
        usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new UsuarioDAOImpl.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(Usuario usuario) {
                if (usuario == null) { // Si no se encuentra el usuario
                    hideLoadingIndicator(); // Ocultar indicador de carga
                    Toast.makeText(LoginPrincipalUI.this, "No hay ninguna cuenta asociada a este correo.", Toast.LENGTH_LONG).show();
                } else {
                    // Iniciar sesión con el correo y la contraseña proporcionados
                    mAuth.signInWithEmailAndPassword(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                usuario.setStatus("logged in");
                                CommonServiceUtilities.guardarUsuario(LoginPrincipalUI.this, usuario);
                                loguearUser(usuario);
                                finish();
                                startActivity(new Intent(LoginPrincipalUI.this, MenuUI.class)); // Abrir actividad del menú
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace(System.out);
                            String errorMessage = null;
                            int duration = Toast.LENGTH_SHORT;

                            // Manejar diferentes tipos de errores de inicio de sesión
                            if (Objects.requireNonNull(e.getMessage()).contains("The email address is badly formatted.")) {
                                errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
                                edtCorreo.requestFocus();
                            } else if (e.getMessage().contains("The supplied auth credential is incorrect, malformed or has expired.")) {
                                errorMessage = "Verifique nuevamente su contraseña por favor.";
                                edtContrasena.setText("");
                                edtContrasena.requestFocus();
                            } else if (e.getMessage().contains("We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")) {
                                errorMessage = "Se ha bloqueado temporalmente el acceso a tu cuenta debido a demasiados intentos de inicio de sesión fallidos. Por favor, restablece tu contraseña o inténtalo más tarde.";
                                duration = Toast.LENGTH_LONG;
                            } else {
                                errorMessage = "Lo sentimos, ha ocurrido un error.";
                            }


                            Toast.makeText(LoginPrincipalUI.this, errorMessage, duration).show();
                            hideLoadingIndicator();
                        }
                    });
                }
            }
        });

    }

    private void abrirMenu() {
        Intent intent = new Intent(LoginPrincipalUI.this, MenuUI.class);
        startActivity(intent);
        finish();
    }

    private void loguearUser(Usuario usuario) {
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, LoginPrincipalUI.this);
        usuarioDAO.updateOnFireStore();
    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        CommonServiceUtilities.showLoadingIndicator(LoginPrincipalUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    private void hideLoadingIndicator() {
        CommonServiceUtilities.hideLoadingIndicator(LoginPrincipalUI.this, loadingLayout, loadingIndicator);
    }
}