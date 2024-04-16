package com.qromarck.reciperu.Interfaces;

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

import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

public class MenuUI extends AppCompatActivity {
    private Button VerMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                    VerMapa = findViewById(R.id.btnVerMapa);

                    // Configura un listener para el botón
                    VerMapa.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Crea un Intent para abrir otra actividad
                            Intent intent = new Intent(MenuUI.this, ReciMapsUI.class);

                            // Inicia la segunda actividad
                            startActivity(intent);
                        }
                    });
            return insets;
        });

        Intent intent = getIntent();
        String nombreUsuario = DataAccessUtilities.usuario.getFull_name();

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
        Intent intent = new Intent(MenuUI.this, PrincipalUI.class); // Reemplaza `PrincipalUI` con el nombre de tu actividad principal
        DataAccessUtilities.usuario = null;
        startActivity(intent);
        // Finaliza la actividad actual
        finish();
    }

}