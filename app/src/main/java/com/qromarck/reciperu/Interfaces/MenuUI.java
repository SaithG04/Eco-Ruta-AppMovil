package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qromarck.reciperu.DAO.DAOImplements.*;
import com.qromarck.reciperu.DAO.*;
import com.qromarck.reciperu.Entity.*;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MenuUI extends AppCompatActivity implements Serializable {

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;  // Añadir esta constante
    private static final int SCROLL_DELAY = 10;
    private static final int SCROLL_DELAY_FAST = 5;
    private static final int SCROLL_INCREMENT = 20;
    private static final int STOP_DURATION = 2000;
    private static boolean exit;
    //public static String typeChange = "";

    // UI Components
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator, progressBar;
    private HorizontalScrollView horizontalScrollView;
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private ImageView imgUser, btnSelectImage, imgloading;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private TextView reci, progressText;
    private Button checkUpdateButton;

    private final Handler handler = new Handler();
    private int scrollMax;
    private int scrollPos = 0;
    private final int[] scrollPositions = {0, 1600, 3100, 4800};
    private int currentPosIndex = 0;
    private final String UPLOAD_URL = "https://reciperu2024.000webhostapp.com/upload.php";
    private final String KEY_IMAGE = "foto";
    private final String KEY_NAME = "nombre";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_ui);

        // Initialize UI components
        initializeUIComponents();

        // Set up listeners
        setUpListeners();

        // Load user information and images
        loadUserInfo();

        // Scroll handling
        handleAutoScroll();

        // Notifications
        handleNotifications();

        // Shop button listener
        handleShopButton();

        // Solicitar permiso de almacenamiento al iniciar la actividad
        checkStoragePermission();
    }

    private void initializeUIComponents() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imagen...");
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imgUser = findViewById(R.id.userImageView);
        imgloading = findViewById(R.id.loading_profilegif);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btn_menu);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        checkUpdateButton = findViewById(R.id.checkUpdateButton);
        MenuUIManager.getInstance().setMenuUI(this);
    }

    private void setUpListeners() {
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    handleNavigationItemSelected(item);
                    return true;
                }
            });
        } else {
            Log.e("MenuUI", "NavigationView is null");
        }

        Button verMapa = findViewById(R.id.btnVerMapa);
        verMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMapButton();
            }
        });

        Button cerrarSesionButton = findViewById(R.id.btnCerrarSesion);
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        Button scan_btn = findViewById(R.id.btnCamara);
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleScanButton();
            }
        });

        checkUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateUtilities.checkForUpdate(MenuUI.this, progressBar, progressText);
            }
        });
    }

    private void loadUserInfo() {
        Usuario usuarioLogged = InterfacesUtilities.recuperarUsuario(MenuUI.this);
        String id = usuarioLogged.getId();
        new GetImageTask(MenuUI.this, imgUser, imgloading).execute(id);
        inicializarUsuario();
    }

    private void handleAutoScroll() {
        horizontalScrollView.post(() -> {
            scrollMax = horizontalScrollView.getChildAt(0).getMeasuredWidth() - getWindowManager().getDefaultDisplay().getWidth();
            horizontalScrollView.scrollTo(0, 0); // Asegura que el scroll siempre inicie desde el borde izquierdo
            autoScroll();
        });
    }

    private void handleNotifications() {
        if (NotificationUtilities.areNotificationsEnabled(getApplicationContext())) {
            showInitialsNotifications();
        } else {
            DialogUtilities.showNotificationSettingsDialog(MenuUI.this);
        }
    }

    private void handleShopButton() {
        ImageView btnShop = findViewById(R.id.imgShop);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionUI.destino = ReciShop.class;
                startActivity(new Intent(MenuUI.this, TransitionUI.class));
            }
        });
    }

    private void handleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_reportar) {
            startActivity(new Intent(MenuUI.this, ReportarUI.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_rutas) {
            startActivity(new Intent(MenuUI.this, RutasUI.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_cambiarcontra) {
            startActivity(new Intent(MenuUI.this, RestablecerContraMenuUser.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void handleMapButton() {
        showLoadingIndicator();
        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    hideLoadingIndicator();
                    showEnableGPSDialog();
                } else {
                    exit = false;
                    TransitionUI.destino = ReciMapsUI.class;
                    startActivity(new Intent(MenuUI.this, TransitionUI.class));
                }
            } else {
                ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                hideLoadingIndicator();
            }
        } else {
            hideLoadingIndicator();
            DialogUtilities.showNoInternetDialog(MenuUI.this);
        }
    }

    private void handleScanButton() {
        showLoadingIndicator();
        if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            hideLoadingIndicator();
            ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                startBarcodeScanning();
            } else {
                hideLoadingIndicator();
                DialogUtilities.showNoInternetDialog(MenuUI.this);
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione Imagen"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if (bitmap == null) {
            Log.e("Error de imagen", "Bitmap es nulo");
            Toast.makeText(MenuUI.this, "Error desconocido", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(MenuUI.this, response, Toast.LENGTH_SHORT).show();
                        Usuario nombreUsuarioLogged = InterfacesUtilities.recuperarUsuario(MenuUI.this);
                        String id = nombreUsuarioLogged.getId();
                        new GetImageTask(MenuUI.this, imgUser, imgloading).execute(id);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MenuUI.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Usuario usuarionombre = InterfacesUtilities.recuperarUsuario(MenuUI.this);
                String id = usuarionombre.getId();
                params.put(KEY_NAME, id);
                params.put(KEY_IMAGE, imageString);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                if (bitmap != null) {
                    uploadImage();
                } else {
                    Toast.makeText(MenuUI.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                handleQRScan(contents);
            } else {
                hideLoadingIndicator();
            }
        }
    }

    private void handleQRScan(String contents) {
        String sede = "26 de Octubre";
        QR qr = new QR();
        qr.setSede(sede);
        QrDAO qrDAO = new QrDAOImpl(qr);
        qrDAO.getQROnFireBase(qr.getSede(), new OnSuccessListener<List<QR>>() {
            @Override
            public void onSuccess(List<QR> qrs) {
                QR qr = qrs.get(0);
                if (qr == null) {
                    QRError();
                } else {
                    int hashPassword = InterfacesUtilities.hashPassword(contents, qr.getSalt());
                    if (hashPassword == qr.getHashed_code()) {
                        handleQRSuccess(qr);
                    } else {
                        QRError();
                    }
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace(System.out);
                hideLoadingIndicator();
            }
        });
    }

    private void handleQRSuccess(QR qr) {
        Map<String, Object> time = new HashMap<>();
        time.put("id", "1");
        time.put("time", ServerValue.TIMESTAMP);

        DataAccessUtilities.insertOnFireStoreRealtime("time", "1", time, new DataAccessUtilities.OnInsertionListener() {
            @Override
            public void onInsertionSuccess() {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("time").child("1");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            boolean isPosible = false;
                            Usuario usuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
                            Timestamp lastScanDate = usuario.getLast_scan_date();
                            Timestamp registroDate = usuario.getRegistro_date();

                            Object timeGet = Objects.requireNonNull(dataSnapshot.child("time").getValue());
                            Long timeMillis = (Long) timeGet;
                            Date date = new Date(timeMillis);
                            Timestamp actualDate = new Timestamp(date);

                            if (InterfacesUtilities.compareDatesWithoutHMS(actualDate, registroDate)) {
                                if (lastScanDate.equals(registroDate)) {
                                    isPosible = true;
                                }
                            } else {
                                if (!InterfacesUtilities.compareDatesWithoutHMS(actualDate, lastScanDate)) {
                                    isPosible = true;
                                }
                            }

                            if (isPosible) {
                                sumarpuntos(qr.getPoints_value());
                            } else {
                                Toast.makeText(getApplicationContext(), "Podrás acumular más puntos mañana.", Toast.LENGTH_SHORT).show();
                                hideLoadingIndicator();
                            }
                        } catch (Exception exception) {
                            Toast.makeText(getApplicationContext(), "Algo salió mal.", Toast.LENGTH_SHORT).show();
                            exception.printStackTrace(System.out);
                            hideLoadingIndicator();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseDatabase", "Error al obtener el tiempo: " + databaseError.getMessage());
                        hideLoadingIndicator();
                    }
                });
            }

            @Override
            public void onInsertionError(String errorMessage) {
                Log.e("Firebase", errorMessage);
                hideLoadingIndicator();
            }
        });
    }

    private void autoScroll() {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels; // Ancho de la pantalla en píxeles
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scrollPos >= scrollMax) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            autoScrollReverse();
                        }
                    }, STOP_DURATION);
                } else {
                    if (currentPosIndex < scrollPositions.length && scrollPos >= screenWidth * currentPosIndex) {
                        currentPosIndex++;
                        handler.postDelayed(this, STOP_DURATION);
                        return;
                    }
                    if (currentPosIndex < scrollPositions.length) {
                        int nextPos = screenWidth * currentPosIndex;
                        scrollPos = Math.min(scrollPos + SCROLL_INCREMENT, nextPos);
                    } else {
                        scrollPos += SCROLL_INCREMENT;
                    }
                    horizontalScrollView.scrollTo(scrollPos, 0);
                    handler.postDelayed(this, SCROLL_DELAY);
                }
            }
        }, SCROLL_DELAY);
    }

    private void autoScrollReverse() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scrollPos <= 0) {
                    currentPosIndex = 0;
                    autoScroll();
                } else {
                    scrollPos -= SCROLL_INCREMENT;
                    horizontalScrollView.scrollTo(scrollPos, 0);
                    handler.postDelayed(this, SCROLL_DELAY_FAST);
                }
            }
        }, SCROLL_DELAY_FAST);
    }

    private void showInitialsNotifications() {
        String notificationChannelID = EcoNotification.createNotificationChannel(getApplicationContext(), 1, "Menu", "Description");
        NotificationUtilities.showNotification(getApplicationContext(), 1, notificationChannelID, "¡Bienvenido de nuevo!", "¡Gracias por utilizar nuestra aplicación!");
        NotificationUtilities.showNotification(getApplicationContext(), 2, notificationChannelID, "¡Recuerda botar tu basura!", "Ayuda a mantener limpio el ambiente.");
    }

    private void startBarcodeScanning() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(MenuUI.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Escanear un QR");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //typeChange = "";
        if (exit) {
            finishAffinity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, continuar con la operación
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado, mostrar un mensaje al usuario
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            hideLoadingIndicator();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    hideLoadingIndicator();
                    showEnableGPSDialog();
                } else {
                    TransitionUI.destino = ReciMapsUI.class;
                    startActivity(new Intent(MenuUI.this, TransitionUI.class));
                }
            } else {
                Toast.makeText(MenuUI.this, "Para ver el mapa, necesitas conceder los permisos de ubicación.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //typeChange = "deslogueo";
                showLoadingIndicator();
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    cerrarSesion();
                } else {
                    hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(MenuUI.this);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private boolean isGPSEnabled() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE) == null || !((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showEnableGPSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Desactivado");
        builder.setMessage("Para continuar, active el GPS en su dispositivo.");
        builder.setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void cerrarSesion() {
        Usuario usuario = InterfacesUtilities.recuperarUsuario(MenuUI.this);
        usuario.setStatus("logged out");
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario);
        usuarioDAO.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
            @Override
            public void onUpdateComplete() {
                FirebaseAuth.getInstance().signOut();
                InterfacesUtilities.guardarUsuario(getApplicationContext(), null);

                TransitionUI.destino = LoginUI.class;
                startActivity(new Intent(getApplicationContext(), TransitionUI.class));
                finish();
            }

            @Override
            public void onUpdateError(String errorMessage) {
                Toast.makeText(getApplicationContext(), "Error al cerrar sesión.", Toast.LENGTH_SHORT).show();
                hideLoadingIndicator();
                Log.w("ERROR", errorMessage);
            }
        });
    }

    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }

    public void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }

    public void sumarpuntos(int puntos) {
        Usuario systemUser = InterfacesUtilities.recuperarUsuario(getApplicationContext());
        int ptosactuales = systemUser.getPuntos();
        ptosactuales += puntos;
        systemUser.setPuntos(ptosactuales);
        systemUser.setLast_scan_date(null);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(systemUser);
        //typeChange = "sumaptos";
        usuarioDAO.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
            @Override
            public void onUpdateComplete() {
                usuarioDAO.getUserOnFireBase(systemUser.getId(), new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        Usuario usuario = usuarios.get(0);
                        InterfacesUtilities.guardarUsuario(getApplicationContext(), usuario);
                        getReci().setText(String.valueOf(usuario.getPuntos()));
                        hideLoadingIndicator();
                        Toast.makeText(getApplicationContext(), "Puntos Agregados Correctamente", Toast.LENGTH_SHORT).show();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al agregar puntos.", Toast.LENGTH_SHORT).show();
                        hideLoadingIndicator();
                        e.printStackTrace(System.out);
                    }
                });
            }

            @Override
            public void onUpdateError(String errorMessage) {
                // Handle update error
            }
        });
    }

    private void inicializarUsuario() {
        Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(MenuUI.this);

        if (userLoggedOnSystem != null) {
            String nombreUsuario = userLoggedOnSystem.getFull_name();
            TextView txvnombreUSER = findViewById(R.id.txvUSERNAME);
            txvnombreUSER.setText(String.format("Bienvenido, %s", nombreUsuario));

            int recipoints = userLoggedOnSystem.getPuntos();
            reci = findViewById(R.id.txvReciPoints);
            reci.setText(String.valueOf(recipoints));
        } else {
            Log.e("MenuUI", "Usuario no disponible");
        }
    }

    private void QRError() {
        Toast.makeText(getApplicationContext(), "QR INVALIDO", Toast.LENGTH_SHORT).show();
        hideLoadingIndicator();
    }

    public TextView getReci() {
        return reci;
    }
}
