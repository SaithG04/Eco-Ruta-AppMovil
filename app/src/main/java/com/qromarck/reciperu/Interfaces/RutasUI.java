package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qromarck.reciperu.R;

public class RutasUI extends AppCompatActivity {

    private Button btnSendImage1, btnSendImage2, btnSendImage3, btnSendImage4, btnSendImage5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rutas_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSendImage1 = findViewById(R.id.btnRuta1);
        btnSendImage2 = findViewById(R.id.btnRuta2);
        btnSendImage3 = findViewById(R.id.btnRuta3);
        btnSendImage4 = findViewById(R.id.btnRuta4);
        btnSendImage5 = findViewById(R.id.btnRuta5);

        btnSendImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage(R.drawable.ruta_1); // Cambia 'image1' por el recurso de tu imagen
            }
        });

        btnSendImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage(R.drawable.ruta_2); // Cambia 'image2' por el recurso de tu imagen
            }
        });

        btnSendImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage(R.drawable.ruta_3); // Cambia 'image2' por el recurso de tu imagen
            }
        });

        btnSendImage4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage(R.drawable.ruta_4); // Cambia 'image2' por el recurso de tu imagen
            }
        });

    }

    private void sendImage(int imageResource) {
        Intent intent = new Intent(RutasUI.this, MapaRutasUI.class);
        intent.putExtra("image_resource", imageResource);
        startActivity(intent);
    }

}