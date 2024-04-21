package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        obtenerUbicacionYAgregarALaBaseDeDatos(DataAccessUtilities.usuario.getId());
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
//                    saveUserLocation(location.getLatitude(), location.getLongitude());
                    // Escuchar cambios en la base de datos de usuarios
                    startListeningToUsersLocations();
                }
            });
        }
    }

    private void startListeningToUsersLocations() {
        usersLocationListener = firestore.collection("usuarios").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            System.out.println(usersLocationListener);
            for (DocumentChange dc : value.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                    try {
                        // Obtener la ubicación del usuario
                        String userId = dc.getDocument().getId();
                        double latitude = dc.getDocument().getDouble("last_latitude");
                        double longitude = dc.getDocument().getDouble("last_longitude");

                        Date date = new Date();
                        Timestamp timestamp= new Timestamp(date);

                        // Actualizar o agregar marcador en el mapa
                        LatLng userLocation = new LatLng(latitude, longitude);
                        if (userId.equals(currentUserMarker.getTag())) {
                            // Si es el usuario actual, actualizar la posición del marcador
                            currentUserMarker.setPosition(userLocation);
                            guardarUbicacionEnFirebase(userId, latitude, longitude, timestamp);
                        } else {
                            // Si es otro usuario, agregar un nuevo marcador
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(userLocation));
                            marker.setTag(userId);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace(System.out);
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


    private void obtenerUbicacionYAgregarALaBaseDeDatos(String userId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            guardarUbicacionEnFirebase(userId, latitude, longitude, new Timestamp(new Date()));
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void guardarUbicacionEnFirebase(String userId, double latitude, double longitude, Timestamp timestamp) {

        Usuario usuario = DataAccessUtilities.usuario;
        String id = usuario.getId();
        String full_name = usuario.getFull_name();
        String email = usuario.getEmail();
        Date registro_date = usuario.getRegistro_date();
        String status = usuario.getStatus();
        String type = usuario.getType();

        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("id", id);
        ubicacion.put("full_name", full_name);
        ubicacion.put("email", email);
        ubicacion.put("registro_date", registro_date);
        ubicacion.put("status", status);
        ubicacion.put("type", type);
        ubicacion.put("last_latitude", latitude);
        ubicacion.put("last_longiteude", longitude);
        ubicacion.put("last_update_ubication_date", timestamp);

        firestore.collection("usuarios")
                .document(userId)
                .set(ubicacion)
                .addOnSuccessListener(aVoid -> {
                    System.out.println(ubicacion);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace(System.out);
                });
    }

    // Método para guardar la ubicación del usuario en Firebase
//    private void saveUserLocation(double latitude, double longitude) {
//        firestore.collection("usuarios").document(DataAccessUtilities.usuario.getId()).set(new UserLocation(latitude, longitude));
//    }
}


