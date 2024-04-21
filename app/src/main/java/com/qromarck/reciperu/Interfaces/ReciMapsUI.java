package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.CommonServiceUtilities;

import java.util.Date;

public class ReciMapsUI extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;
    private boolean mapReady = false;
    private Usuario userLoggedOnSystem;
    private LocationManager locationManager;
    private AlertDialog dialog;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reci_maps_ui);

        userLoggedOnSystem = CommonServiceUtilities.recuperarUsuario(ReciMapsUI.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Verificar los permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Si los permisos están garantizados, actualizar la ubicación inicial
            updateInitialUbication();
        } else {
            // Si no están garantizados, solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        mapReady = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateInitialUbication();
            } else {
                finish();
                startActivity(new Intent(ReciMapsUI.this, MenuUI.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mapReady) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    300000,   // Intervalo de actualización en milisegundos
                    10, this); // Distancia mínima de cambio en metros

            // Agregar un pequeño retraso antes de obtener la última ubicación conocida
            new Handler().postDelayed(this::updateInitialUbication, 1000);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    private void updateInitialUbication() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println("LOCATIONCHANGED");
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(18.0f)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        userLoggedOnSystem.setLast_latitude(location.getLatitude());
        userLoggedOnSystem.setLast_longitude(location.getLongitude());
        userLoggedOnSystem.setLast_update_ubication_date(new Timestamp(new Date()));

        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userLoggedOnSystem, ReciMapsUI.this);
        usuarioDAO.updateOnFireStore();
        CommonServiceUtilities.guardarUsuario(ReciMapsUI.this, userLoggedOnSystem);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (!isFinishing() && !isDestroyed()) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!isFinishing() && !isDestroyed()) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("Por favor, habilite su ubicación para continuar.")
                        .setPositiveButton("Configuración", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            finish();
                            startActivity(new Intent(ReciMapsUI.this, MenuUI.class));
                        })
                        .show();
            }
        }
    }
}
