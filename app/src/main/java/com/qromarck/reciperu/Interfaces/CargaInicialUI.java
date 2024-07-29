package com.qromarck.reciperu.Interfaces;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;
import com.qromarck.reciperu.Utilities.NotificationUtilities;
import com.qromarck.reciperu.Utilities.UpdateUtilities;

public class CargaInicialUI extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT = 2000; // 2 segundos
    FrameLayout loadingLayout;
    ProgressBar progressBar;
    TextView progressText;


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

        loadingLayout = findViewById(R.id.loadingLayout);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        //UpdateUtilities.checkForUpdate(this, progressBar, progressText, loadingLayout);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("TOKEN", "El token es: " + token);
                        //Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
                    }
                });


        cargar();
    }

    public void cargar() {
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {*/
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {

                    UpdateUtilities.checkForUpdate(CargaInicialUI.this, progressBar, progressText, loadingLayout);


                    /*
                    if(!NotificationUtilities.areNotificationsEnabled(getApplicationContext())){
                        DialogUtilities.showNotificationSettingsDialog(CargaInicialUI.this);
                    }else{
                        Usuario usuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
                        if (usuario != null) {
                            // El usuario está logueado
                            startActivity(new Intent(CargaInicialUI.this, MenuUI.class));
                            finish();
                        } else {
                            startActivity(new Intent(CargaInicialUI.this, LoginUI.class));
                            finish();
                        }
                    }*/
                } else {
                    DialogUtilities.showNoInternetDialog(CargaInicialUI.this);
                }
                /*
            }
        }, SPLASH_SCREEN_TIMEOUT);*/
    }

}