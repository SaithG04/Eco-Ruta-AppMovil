package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

public class TransitionUI extends AppCompatActivity {

    public static int SPLASH_SCREEN_TIMEOUT = 500;
    public static Class<?> destino = null;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destino = null;
        SPLASH_SCREEN_TIMEOUT = 500;
    }

    public void cargar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(destino!=null){
                    Log.d("DEBUG", "DESTINO: "  + destino.getSimpleName());
                    startActivity(new Intent(TransitionUI.this, destino));
                    finish();
                }else{
                    Log.e("ERROR", "ORIGEN DESCONOCIDO");
                    finish();
                }
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}