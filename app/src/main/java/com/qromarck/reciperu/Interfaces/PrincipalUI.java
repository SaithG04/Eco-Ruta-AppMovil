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
import android.widget.Toast;


import java.security.MessageDigest;

import javax.xml.transform.sax.SAXResult;

public class PrincipalUI extends AppCompatActivity {

    private Button btnRegistrarse;
    private EditText edtCorreo, edtContrasena;
    private Button btnLogin;
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
        Usuario userSearched = new Usuario();
        userSearched.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched, PrincipalUI.this);
        usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new UsuarioDAOImpl.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(Usuario usuario) {
                if (usuario == null) {
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
                            } else {
                                errorMessage = "Lo sentimos, ha ocurrido un error.";
                            }
                            Toast.makeText(PrincipalUI.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    @Deprecated
    private void login(String correo, String password) {

        Usuario userFind = new Usuario();
        userFind.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userFind, PrincipalUI.this);
        usuarioDAO.getUserBy(userFind.getEmail(), new DataAccessUtilities.OnDataRetrievedOneListener<Usuario>() {
            @Override
            public void onDataRetrieved(Usuario userReceived) {
                if (userReceived != null) {
                    byte[] inputHashedPassword = CommonServiceUtilities.hashPassword(password, userReceived.getSalt());
                    if (!MessageDigest.isEqual(userReceived.getHashedPassword(), inputHashedPassword)) {
                        // Mostrar mensaje de error si la contraseña es incorrecta
                        Toast.makeText(getApplicationContext(), "Contraseña incorrecta. Verifique nuevamente.", Toast.LENGTH_SHORT).show();
                        edtContrasena.setText("");
                        edtCorreo.requestFocus();
                        return; // Finalizar el método si la contraseña es incorrecta
                    }
                    // Crear un Intent para iniciar la nueva actividad
                    Intent intent = new Intent(PrincipalUI.this, MenuUI.class);
                    edtCorreo.setText("");
                    edtContrasena.setText("");
                    edtCorreo.requestFocus();
                    //Guardar logueo
                    DataAccessUtilities.usuario = userReceived;
                    // Iniciar la nueva actividad
                    startActivity(intent);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Manejar el error en caso de problemas con la solicitud
                if (errorMessage.contains("Error de red")) {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    edtCorreo.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "No hay usuarios registrados con ese correo.", Toast.LENGTH_SHORT).show();
                }
                edtContrasena.setText("");
                edtCorreo.requestFocus();
            }
        });
    }
}