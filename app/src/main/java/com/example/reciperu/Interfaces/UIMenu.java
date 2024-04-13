package com.example.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reciperu.MainActivity;
import com.example.reciperu.R;

public class UIMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_uimenu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String nombreUsuario = intent.getStringExtra("usuario");

        TextView txvnombreUSER = findViewById(R.id.txvUSERNAME);

        txvnombreUSER.setText("Bienvenido, " + nombreUsuario);

        // Obtén el botón de cerrar sesión
        Button cerrarSesionButton = findViewById(R.id.btnCerrarSesion);

        // Configura el OnClickListener para el botón de cerrar sesión
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    // Método para manejar el cierre de sesión
    private void cerrarSesion() {
        // Inicia la actividad principal
        Intent intent = new Intent(UIMenu.this, MainActivity.class); // Reemplaza `MainActivity` con el nombre de tu actividad principal
        startActivity(intent);

        // Finaliza la actividad actual
        finish();
    }
}