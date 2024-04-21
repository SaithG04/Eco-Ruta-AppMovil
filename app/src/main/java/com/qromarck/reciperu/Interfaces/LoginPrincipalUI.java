package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qromarck.reciperu.R;

import java.util.HashMap;
import java.util.Map;

public class LoginPrincipalUI extends AppCompatActivity {

    private EditText edtCorreo;
    private EditText edtContrasena;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_ui);

        edtCorreo = findViewById(R.id.edtCorreoLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        Button btnRegistrarse = findViewById(R.id.btnRegistrar);
        Button btnLogin = findViewById(R.id.btnLoginLOG);
        ImageView viewRegGoogle = findViewById(R.id.viewRegistrarGoogle);

        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firestore = FirebaseFirestore.getInstance();

        btnRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPrincipalUI.this, RegistroUsuarioUI.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String correo = edtCorreo.getText().toString();
            String password = edtContrasena.getText().toString();
            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginPrincipalUI.this, "¡Ey, faltan datos!", Toast.LENGTH_SHORT).show();
            } else {
                loginOnFireBase(correo, password);
            }
        });

        viewRegGoogle.setOnClickListener(v -> {
            // Agregar aquí la lógica para iniciar sesión con Google
        });
    }

    private void loginOnFireBase(String correo, String password) {
        showLoadingIndicator();

        mAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            obtenerUbicacionYAgregarALaBaseDeDatos(user.getUid());
                        }
                    } else {
                        hideLoadingIndicator();
                        Toast.makeText(LoginPrincipalUI.this, "Error al iniciar sesión. Por favor, verifique sus credenciales.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void obtenerUbicacionYAgregarALaBaseDeDatos(String userId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            guardarUbicacionEnFirebase(userId, latitude, longitude);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void guardarUbicacionEnFirebase(String userId, double latitude, double longitude) {
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitude", latitude);
        ubicacion.put("longitude", longitude);
        ubicacion.put("timestamp", System.currentTimeMillis());

        firestore.collection("users_locations")
                .document(userId)
                .set(ubicacion)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginPrincipalUI.this, "Ubicación guardada correctamente", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                    abrirMenu();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginPrincipalUI.this, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                });
    }

    private void showLoadingIndicator() {
        loadingLayout.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingLayout.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void abrirMenu() {
        Intent intent = new Intent(LoginPrincipalUI.this, MenuUI.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido, obtener ubicación y agregar a la base de datos
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    obtenerUbicacionYAgregarALaBaseDeDatos(user.getUid());
                }
            } else {
                // Permiso de ubicación denegado
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                hideLoadingIndicator();
            }
        }
    }
}
