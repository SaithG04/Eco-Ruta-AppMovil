package com.qromarck.reciperu.Interfaces;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        Button enviar = findViewById(R.id.btnEnviarGmail);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtén la referencia al EditText donde se ingresa el correo
                EditText gmailEditText = findViewById(R.id.edtGmail);

                // Obtiene el texto ingresado en el EditText
                String email = gmailEditText.getText().toString().trim();

                // Verifica si el campo de correo electrónico no está vacío
                if (email.isEmpty()) {
                    Toast.makeText(RestablecerContra.this, "Ingrese su Correo, Por favor.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validar el formato del correo electrónico usando una expresión regular
                if (!isValidEmail(email)) {
                    Toast.makeText(RestablecerContra.this, "Ingrese un correo electrónico válido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Inicializa Firebase Authentication
                mAuth = FirebaseAuth.getInstance();

                // Envía el correo de restablecimiento de contraseña
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Correo de restablecimiento enviado con éxito
                                    Toast.makeText(RestablecerContra.this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_SHORT).show();
                                    finish(); // Cierra la actividad actual
                                } else {
                                    // Error al enviar el correo de restablecimiento
                                    Toast.makeText(RestablecerContra.this, "Error al enviar el correo de restablecimiento. " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            // Método para validar el formato del correo electrónico usando expresión regular
            private boolean isValidEmail(String email) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                return email.matches(emailPattern);
            }

        });

    }
}