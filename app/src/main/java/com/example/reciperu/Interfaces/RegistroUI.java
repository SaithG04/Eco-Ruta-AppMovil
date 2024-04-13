package com.example.reciperu.Interfaces;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reciperu.R;

public class RegistroUI extends AppCompatActivity {
    private EditText edtUsuario, edtCorreo, edtContrasena;
    private Button registrarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_ui);

        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        registrarButton = findViewById(R.id.btnREG);

        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = edtUsuario.getText().toString();
        String correo = edtCorreo.getText().toString();
        String contrasena = edtContrasena.getText().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show();
        } else if (correo.isEmpty()) {
            Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show();
        } else if (contrasena.isEmpty()) {
            Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
        } else {
            // Ejecutar la tarea de inserción de usuario en segundo plano
            MySQLService.insertData(getApplicationContext(), nombre, correo, contrasena);
        }
    }
}
