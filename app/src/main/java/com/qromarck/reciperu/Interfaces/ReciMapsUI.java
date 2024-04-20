package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.qromarck.reciperu.R;

public class ReciMapsUI extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private Marker currentUserMarker;
    private ListenerRegistration usersLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reci_maps_ui);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Solicitar permisos de ubicación si es necesario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            enableLocation();
        }
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
                    currentUserMarker = googleMap.addMarker(new MarkerOptions().position(currentLocation));
                    // Guardar la ubicación del usuario en Firebase
                    saveUserLocation(location.getLatitude(), location.getLongitude());
                    // Escuchar cambios en la base de datos de usuarios
                    startListeningToUsersLocations();
                }
            });
        }
    }

    private void startListeningToUsersLocations() {
        usersLocationListener = firestore.collection("users_locations").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            for (DocumentChange dc : value.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                    // Obtener la ubicación del usuario
                    String userId = dc.getDocument().getId();
                    double latitude = dc.getDocument().getDouble("latitude");
                    double longitude = dc.getDocument().getDouble("longitude");

                    // Actualizar o agregar marcador en el mapa
                    LatLng userLocation = new LatLng(latitude, longitude);
                    if (userId.equals(currentUserMarker.getTag())) {
                        // Si es el usuario actual, actualizar la posición del marcador
                        currentUserMarker.setPosition(userLocation);
                    } else {
                        // Si es otro usuario, agregar un nuevo marcador
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(userLocation));
                        marker.setTag(userId);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersLocationListener != null) {
            usersLocationListener.remove();
        }
    }

    // Método para guardar la ubicación del usuario en Firebase
    private void saveUserLocation(double latitude, double longitude) {
        // Puedes obtener el ID del usuario de la autenticación de Firebase o de otra manera
        String userId = "user_id"; // Reemplaza esto con el ID del usuario
        firestore.collection("users_locations").document(userId).set(new UserLocation(latitude, longitude));
    }
}


