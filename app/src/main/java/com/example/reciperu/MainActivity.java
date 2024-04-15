package com.example.reciperu;


import android.os.Bundle;

import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import android.content.Intent;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.example.reciperu.Interfaces.RegistroUI;



import android.widget.EditText;


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private Button btnRegistrarse;

    private EditText edtusuario, edtContrasena;
    private Button btnLogin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        //Referenciar boton Regisrarse
        btnRegistrarse = findViewById(R.id.btnRegistrar);
        edtusuario = findViewById(R.id.edtUsuarioLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        btnLogin = findViewById(R.id.btnLoginLOG);

        // Configurar el listener del botón
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un Intent para iniciar la nueva actividad
                Intent intent = new Intent(MainActivity.this, RegistroUI.class);
                edtusuario.setText("");
                edtContrasena.setText("");

                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });


        // Configura el listener para el botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para iniciar la nueva actividad
                Intent intent = new Intent(MainActivity.this, ReciMaps.class);
                edtusuario.setText("");
                edtContrasena.setText("");

                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });



    }


}