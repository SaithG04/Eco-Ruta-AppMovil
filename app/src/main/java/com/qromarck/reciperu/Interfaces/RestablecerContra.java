package com.qromarck.reciperu.Interfaces;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

public class RestablecerContra extends AppCompatActivity {
    private FirebaseAuth mAuth;

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

        // Inicializa Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Obtén la referencia al botón de restablecimiento de contraseña en tu diseño XML
        Button resetButton = findViewById(R.id.btnEnviarGmail);

        // Asigna un OnClickListener al botón
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtén el correo electrónico del usuario
                Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(RestablecerContra.this);
                String email = userLoggedOnSystem.getEmail();

                // Envía el correo de restablecimiento de contraseña
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Correo de restablecimiento enviado con éxito
                                    Toast.makeText(RestablecerContra.this,
                                            "Se ha enviado un correo para restablecer tu contraseña",
                                            Toast.LENGTH_SHORT).show();
                                    finish(); // Cierra la actividad actual
                                } else {
                                    // Error al enviar el correo de restablecimiento
                                    Toast.makeText(RestablecerContra.this,
                                            "Error al enviar el correo de restablecimiento. " + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}