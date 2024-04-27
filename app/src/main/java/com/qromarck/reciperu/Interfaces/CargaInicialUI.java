package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

public class CargaInicialUI extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transition_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cargar();
    }

    public void cargar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    Usuario usuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
                    if (usuario != null) {
                        // El usuario está logueado
                        startActivity(new Intent(CargaInicialUI.this, ValidacionUI.class));
                        finish();
                    } else {
                        startActivity(new Intent(CargaInicialUI.this, LoginUI.class));
                        finish();
                    }
                } else {
                    DialogUtilities.showNoInternetDialog(CargaInicialUI.this);
                }
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}