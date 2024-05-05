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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

import java.util.List;

public class RestablecerContra extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_contra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button enviar = findViewById(R.id.btnEnviarGmail);
        mAuth = FirebaseAuth.getInstance();
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingIndicator();
                // Obtén la referencia al EditText donde se ingresa el correo
                EditText gmailEditText = findViewById(R.id.edtGmail);

                // Obtiene el texto ingresado en el EditText
                String email = gmailEditText.getText().toString().trim();

                // Verifica si el campo de correo electrónico no está vacío
                if (email.isEmpty()) {
                    Toast.makeText(RestablecerContra.this, "Ingrese su correo, Por favor.", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                    return;
                }

                // Validar el formato del correo electrónico usando una expresión regular
                if (!InterfacesUtilities.isValidEmail(email)) {
                    Toast.makeText(RestablecerContra.this, "Ingrese un correo válido.", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                    return;
                }

                // Crear un usuario con el correo proporcionado
                Usuario userSearched = new Usuario();
                userSearched.setEmail(email);
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched);

                // Buscar usuario en Firebase
                usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        if (usuarios.isEmpty()) {
                            hideLoadingIndicator(); // Ocultar indicador de carga
                            Toast.makeText(getApplicationContext(), "No hay ninguna cuenta asociada a este correo.", Toast.LENGTH_LONG).show();
                        } else {
                            // Envía el correo de restablecimiento de contraseña
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Correo de restablecimiento enviado con éxito
                                                Toast.makeText(RestablecerContra.this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_SHORT).show();
                                                hideLoadingIndicator();
                                                finish(); // Cierra la actividad actual
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Error al enviar el correo de restablecimiento
                                            hideLoadingIndicator();
                                            Toast.makeText(RestablecerContra.this, "Error al enviar el correo de restablecimiento. ", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace(System.out);
                                        }
                                    });
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace(System.out);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideLoadingIndicator();
        TransitionUI.destino = LoginUI.class;
        Log.d("DEBUG", "FROM: " + RegistroUsuarioUI.class.getSimpleName());
        startActivity(new Intent(RestablecerContra.this, TransitionUI.class));
        finish();
    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(RestablecerContra.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    public void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(RestablecerContra.this, loadingLayout, loadingIndicator);
    }
}