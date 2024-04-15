package com.example.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reciperu.MainActivity;
import com.example.reciperu.R;
import com.example.reciperu.ReciMaps;
import com.example.reciperu.Utilities.DataAccessUtilities;

public class UIMenu extends AppCompatActivity {
    private Button VerMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_uimenu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                    VerMapa = findViewById(R.id.btnVerMapa);

                    // Configura un listener para el botón
                    VerMapa.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Crea un Intent para abrir otra actividad
                            Intent intent = new Intent(UIMenu.this, ReciMaps.class);

                            // Inicia la segunda actividad
                            startActivity(intent);
                        }
                    });
            return insets;
        });

        Intent intent = getIntent();
        String nombreUsuario = DataAccessUtilities.usuario.getUsuario();

        TextView txvnombreUSER = findViewById(R.id.txvUSERNAME);

        txvnombreUSER.setText(String.format("Bienvenido, %s", nombreUsuario));

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
        DataAccessUtilities.usuario = null;
        startActivity(intent);
        // Finaliza la actividad actual
        finish();
    }

}