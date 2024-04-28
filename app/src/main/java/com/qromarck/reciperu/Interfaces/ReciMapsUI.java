package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qromarck.reciperu.DAO.DAOImplements.LocationDAOImpl;
import com.qromarck.reciperu.DAO.LocationDAO;
import com.qromarck.reciperu.Entity.MenuUIManager;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//SONIDO DE PROXIMIDAD
import android.location.Location;
import android.media.MediaPlayer;

public class ReciMapsUI extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;
    private boolean mapReady = false;
    private Usuario userLoggedOnSystem;
    private LocationManager locationManager;
    private AlertDialog dialog;
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    Location lastKnownLocation;
    private Geocoder geocoder;
    private Marker otherUserMarker;
    private final Handler handlerConductor = new Handler();
    private final Handler handlerUser = new Handler();

    //SONIDO PROXIMIDAD
    private MediaPlayer mediaPlayer;
    private static final int DISTANCE_THRESHOLD_METERS = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reci_maps_ui);

        initializeUI();
        checkLocationPermissions();
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
    protected void onResume() {
        super.onResume();
        if (!isConductor()) {
            updateUbicationOfConductor();
        }
        configureUpdates();
        updateLastUbication();
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyDialog();
        handlerUser.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyDialog();
        handlerUser.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyDialog();
        handlerConductor.removeCallbacksAndMessages(null);
        handlerUser.removeCallbacksAndMessages(null);
        startActivity(new Intent(ReciMapsUI.this, MenuUI.class));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TransitionUI.destino = MenuUI.class;
        startActivity(new Intent(ReciMapsUI.this, TransitionUI.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLastUbication();
            } else {
                finish();
                startActivity(new Intent(ReciMapsUI.this, MenuUI.class));
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        updateLastUbication();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastKnownLocation = location;
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if (!isFinishing() && !isDestroyed()) {
            System.out.println("GPS ACTIVADO");
            destroyDialog();
            if (isConductor()) {
                updateMyUbicationAsConductor();
            } else {
                updateUbicationOfConductor();
            }
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if (!isFinishing() && !isDestroyed()) {
            System.out.println("GPS DESACTIVADO");
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                System.out.println(provider);
                if (dialog == null || !dialog.isShowing()) {
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
                            .setCancelable(false) // Evita que se cierre tocando fuera del diálogo
                            .show();
                }
            } else {
                System.out.println(provider);
            }
        }
    }

    public void updateLastUbication() {
        if (!isFinishing() && !isDestroyed()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                moveCamera(location);
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ReciMapsUI.this.updateLastUbication();
                                    }
                                }, 50);

                            }
                        }
                    });
        }
    }

    private void updateMyUbicationAsConductor() {
        handlerConductor.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Llama al método para obtener la ubicación del otro usuario
                updateLastUbication();
                // Programa la próxima actualización después de un intervalo de tiempo (por ejemplo, 1 segundo)
                handlerConductor.postDelayed(this, 1000);
            }
        }, 1000); // Llama al método inicialmente después de 1 segundo
    }

    private void updateUbicationOfConductor() {
        handlerUser.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("Obteniendo ubi del conductor...");
                // Llama al método para obtener la ubicación del otro usuario
                getConductorUbication();
                // Programa la próxima actualización después de un intervalo de tiempo (por ejemplo, 1 segundo)
                handlerUser.postDelayed(this, 3000);
            }
        }, 1000); // Llama al método inicialmente después de 1 segundo
    }

    private void configureUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mapReady) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    6000,   // Intervalo de actualización en milisegundos (5 minutos)
                    5, this); // Distancia mínima de cambio en metros
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    private void moveCamera(Location location) {
        float zoom = 18.0f;
        if (isConductor()) {
            zoom = 15.0f;
        }
        if (location != null) {
            if (googleMap != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentLocation)
                        .zoom(zoom)
                        .build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                saveOnFB(location);
                InterfacesUtilities.guardarUsuario(ReciMapsUI.this, userLoggedOnSystem);
            } else {
                new Handler().postDelayed(this::updateLastUbication, 50);
            }
        } else {
            new Handler().postDelayed(this::updateLastUbication, 50);
        }

    }

    private void saveOnFB(Location location) {

        com.qromarck.reciperu.Entity.Location locationUp = new com.qromarck.reciperu.Entity.Location();
        String city = getCityFromLocation(location, geocoder);
        locationUp.setUserId(userLoggedOnSystem.getId());
        locationUp.setLatitude(location.getLatitude());
        locationUp.setLongitude(location.getLongitude());
        locationUp.setCity(city);
        LocationDAO locationDAO = new LocationDAOImpl(locationUp);
        locationDAO.insertOnFireStore();
    }

    private String getCityFromLocation(Location location, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                if (city != null) {
                    return city;
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    private void getConductorUbication() {
        String otherUserId = "2PEUsE6tkVRlvsm9KmVHVxutmBw1";
        DatabaseReference otherUserLocationRef = FirebaseDatabase.getInstance().getReference("user_locations").child(otherUserId);
        otherUserLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // Obtener la ubicación del conductor desde el DataSnapshot
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    // Agregar el marcador del conductor en el mapa
                    setMarkerOfConductor(latitude, longitude);
                }catch (Exception exception){
                    exception.printStackTrace(System.out);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores de lectura de datos
                Log.e("FirebaseDatabase", "Error al obtener la ubicación del conductor: " + databaseError.getMessage());
            }
        });
    }

    private void setMarkerOfConductor(double latitude, double longitude) {
        if (googleMap != null) {
            LatLng otherUserLocation = new LatLng(latitude, longitude);
            // Cargar el icono personalizado desde los recursos
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.truck_icon);
            //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(otherUserLocation)
                    .title("CONDUCTOR")
                    .icon(icon); // Personalizar el icono del marcador

            // Elimina el marcador anterior del otro usuario si ya existe
            if (otherUserMarker != null) {
                otherUserMarker.remove();
            }

            // Agrega el nuevo marcador del otro usuario en el mapa
            otherUserMarker = googleMap.addMarker(markerOptions);
        }
    }


    private boolean isConductor() {
        return InterfacesUtilities.recuperarUsuario(ReciMapsUI.this).getType().equals("conductor");
    }

    private void destroyDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void initializeUI() {
        userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(ReciMapsUI.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLastUbication();
            if (isConductor()) {
                updateMyUbicationAsConductor();
            } else {
                updateUbicationOfConductor();
            }
        } else {
            requestLocationPermission();
        }
        MenuUI menuUI = MenuUIManager.getInstance().getMenuUI();
        if (menuUI != null) {
            menuUI.hideLoadingIndicator();
        }
    }
}




