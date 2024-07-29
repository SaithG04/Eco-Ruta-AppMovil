package com.qromarck.reciperu.Interfaces;

//Importaciones
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
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

    // Constantes
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    public static final int INSTALL_UNKNOWN_APPS_PERMISSION_REQUEST_CODE = 1003;
    private static final int SCROLL_DELAY = 10;
    private static final int SCROLL_DELAY_FAST = 5;
    private static final int SCROLL_INCREMENT = 20;
    private static final int STOP_DURATION = 2000;
    private static boolean exit;

    // Componentes de la UI
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    private HorizontalScrollView horizontalScrollView;
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private ImageView imgUser, selectImageButton, imgloading;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private TextView reci;
    private Button mapButton, logoutButton, pointsButton;
    private NavigationView navigationView;

    private final Handler handler = new Handler();
    private int scrollMax;
    private int scrollPos = 0;
    //private final int[] scrollPositions = {0, 1600, 3100, 4800};
    private int currentPosIndex = 0;
    private final String UPLOAD_URL = "https://reciperu2024.000webhostapp.com/upload.php";
    private final String KEY_IMAGE = "foto";
    private final String KEY_NAME = "nombre";

    //Método onCreate
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
    }

    //Método onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exit) {
            finishAffinity();
        }
    }

    //Inicialización de componentes
    private void initializeUIComponents() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imagen...");
        selectImageButton = findViewById(R.id.btnSelectImage);
        imgUser = findViewById(R.id.userImageView);
        imgloading = findViewById(R.id.loading_profilegif);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btn_menu);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        navigationView = findViewById(R.id.nav_view);
        mapButton = findViewById(R.id.btnVerMapa);
        logoutButton = findViewById(R.id.btnCerrarSesion);
        pointsButton = findViewById(R.id.btnCamara);
        MenuUIManager.getInstance().setMenuUI(this);
    }

    //Listeners
    private void setUpListeners() {
        selectImageButton.setOnClickListener(new View.OnClickListener() {
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


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMapButton();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtilities.showLogoutConfirmationDialog(MenuUI.this);
            }
        });

        pointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleScanButton();
            }
        });
    }

    //Información del usuario
    private void loadUserInfo() {
        Usuario usuarioLogged = InterfacesUtilities.recuperarUsuario(MenuUI.this);
        String id = usuarioLogged.getId();

        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(usuarioLogged.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> signInMethods = task.getResult().getSignInMethods();
                Log.d("LoginActivity", "SignIn Methods: " + signInMethods);

                if (signInMethods != null && signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                    try {
                        Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(imgUser);
                        selectImageButton.setEnabled(false);
                    }catch (Exception exception){
                        exception.printStackTrace(System.err);
                        imgUser.setImageResource(R.drawable.vectorlogin);
                    }
                } else {
                    new GetImageTask(MenuUI.this, imgUser, imgloading).execute(id);
                }
            }
        });

        inicializarUsuario();
    }

    //Manejo de scroll automático
    private void handleAutoScroll() {
        horizontalScrollView.post(() -> {
            scrollMax = horizontalScrollView.getChildAt(0).getMeasuredWidth() - getWindowManager().getDefaultDisplay().getWidth();
            horizontalScrollView.scrollTo(0, 0);
            autoScroll();
        });
    }

    private void autoScroll() {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
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
                    if (currentPosIndex < 4 && scrollPos >= screenWidth * currentPosIndex) {
                        currentPosIndex++;
                        handler.postDelayed(this, STOP_DURATION);
                        return;
                    }
                    if (currentPosIndex < 4) {
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

    //Manejo de notificaciones
    private void handleNotifications() {
        if (NotificationUtilities.areNotificationsEnabled(getApplicationContext())) {
            showInitialsNotifications();
        } else {
            DialogUtilities.showNotificationSettingsDialog(MenuUI.this);
        }
    }

    private void showInitialsNotifications() {
        String notificationChannelID = EcoNotification.createNotificationChannel(getApplicationContext(), 1, "Menu", "Description");
        NotificationUtilities.showNotification(getApplicationContext(), 1, notificationChannelID, "¡Bienvenido de nuevo!", "¡Gracias por utilizar nuestra aplicación!");
        NotificationUtilities.showNotification(getApplicationContext(), 2, notificationChannelID, "¡Recuerda botar tu basura!", "Ayuda a mantener limpio el ambiente.");
    }

    //Botón de EcoShop
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

    //Manejo de ítems del menú lateral
    private void handleNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_reportar) {
            startActivity(new Intent(MenuUI.this, ReportarUI.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_rutas) {
            startActivity(new Intent(MenuUI.this, RutasUI.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_cambiarcontra) {
            startActivity(new Intent(MenuUI.this, RestablecerContra.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    //Manejo del botón del mapa
    private void handleMapButton() {
        showLoadingIndicator();
        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    hideLoadingIndicator();
                    DialogUtilities.showEnableGPSDialog(this);
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

    private boolean isGPSEnabled() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE) == null || !((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    //Manejo de permisos de almacenamiento y actualizaciones
    /*private void checkPermissionsAndUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            DialogUtilities.showStoragePermissionDialog(this);
        } else {
            checkInstallUnknownAppsPermissionAndUpdate();
        }
    }*/

    /*private void checkInstallUnknownAppsPermissionAndUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                DialogUtilities.showInstallSettingsDialog(this);
            } else {
                //UpdateUtilities.checkForUpdate(MenuUI.this, progressBar, progressText, loadingLayout);
            }
        } else {
            //UpdateUtilities.checkForUpdate(MenuUI.this, progressBar, progressText, loadingLayout);
        }
    }*/

    //Resultado de la actividad
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

    //Manejo de resultados de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show();
                //checkInstallUnknownAppsPermissionAndUpdate();
            } else {
                ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);


                //Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                // Request all storage permissions if any are missing
                //DialogUtilities.showStoragePermissionDialog(this);
            }
        }
        //Manejar permisos para GPS
        else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            hideLoadingIndicator();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    hideLoadingIndicator();
                    DialogUtilities.showEnableGPSDialog(this);
                } else {
                    TransitionUI.destino = ReciMapsUI.class;
                    startActivity(new Intent(MenuUI.this, TransitionUI.class));
                }
            } else {
                Toast.makeText(MenuUI.this, "Para ver el mapa, necesitas conceder los permisos de ubicación.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == INSTALL_UNKNOWN_APPS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //UpdateUtilities.checkForUpdate(this, progressBar, progressText, loadingLayout);
            } else {
                Toast.makeText(this, "Permiso para instalar aplicaciones desconocidas denegado", Toast.LENGTH_SHORT).show();
            }
        }
        /*
        else if (requestCode == ALL_STORAGE_PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                checkInstallUnknownAppsPermissionAndUpdate();
            } else {
                DialogUtilities.showStoragePermissionDialog(this);
            }
        }*/
    }

    //Manejo del escaneo de QR
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

    private void QRError() {
        Toast.makeText(getApplicationContext(), "QR INVALIDO", Toast.LENGTH_SHORT).show();
        hideLoadingIndicator();
    }

    //Manejo de suma y resta de puntos
    public void sumarpuntos(int puntos) {
        Usuario systemUser = InterfacesUtilities.recuperarUsuario(getApplicationContext());
        int ptosactuales = systemUser.getPuntos();
        ptosactuales += puntos;
        systemUser.setPuntos(ptosactuales);
        systemUser.setLast_scan_date(null);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(systemUser);
        usuarioDAO.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
            @Override
            public void onUpdateComplete() {
                usuarioDAO.getUserOnFireBase(systemUser.getId(), new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        Usuario usuario = usuarios.get(0);
                        InterfacesUtilities.guardarUsuario(getApplicationContext(), usuario);
                        reci.setText(String.valueOf(usuario.getPuntos()));
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
            }
        });
    }

    //Inicialización de usuario
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

    //Mostrar Indicador de Carga
    public void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }

    public void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }
    //Manejo del botón de escanear puntos
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

    private void startBarcodeScanning() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(MenuUI.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Escanear un QR");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    //Manejo de selección de imagen desde archivos
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

    public void cerrarSesion() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic("horarios")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.w("ERROR", "Suscripción fallida", task.getException());
                            Toast.makeText(getApplicationContext(), "Ha ocurrido un error inesperado reintente.", Toast.LENGTH_LONG).show();
                        }else{

                            Usuario usuarioLog = InterfacesUtilities.recuperarUsuario(getApplicationContext());
                            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuarioLog);

                            // Buscar usuario en Firebase
                            usuarioDAO.getUserOnFireBase(usuarioLog.getEmail(), new OnSuccessListener<List<Usuario>>() {
                                @Override
                                public void onSuccess(List<Usuario> usuarios) {
                                    if (usuarios.isEmpty()) {
                                        hideLoadingIndicator(); // Ocultar indicador de carga
                                        Toast.makeText(getApplicationContext(), "Tu cuenta ha sido eliminada o deshabilitada.", Toast.LENGTH_LONG).show();
                                        proceedToLogout();
                                    } else {
                                        //Usuario usuario = InterfacesUtilities.recuperarUsuario(MenuUI.this);
                                        usuarioLog.setStatus("logged out");
                                        UsuarioDAO usuarioDAO2 = new UsuarioDAOImpl(usuarioLog);
                                        usuarioDAO2.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
                                            @Override
                                            public void onUpdateComplete() {
                                                FirebaseAuth.getInstance().signOut();

                                                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                                                if (account != null) {
                                                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                            .requestIdToken(getString(R.string.default_web_client_id))
                                                            .requestEmail()
                                                            .build();
                                                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                                                    mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                                                        // Cerrar sesión de Google completada
                                                        proceedToLogout();
                                                    });
                                                } else {
                                                    // No hay cuenta de Google logueada
                                                    proceedToLogout();
                                                }
                                            }

                                            @Override
                                            public void onUpdateError(String errorMessage) {
                                                Toast.makeText(getApplicationContext(), "Error al cerrar sesión.", Toast.LENGTH_SHORT).show();
                                                hideLoadingIndicator();
                                                Log.w("ERROR", errorMessage);
                                            }
                                        });
                                    }
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace(System.out);
                                }
                            });
                        }
                    }
                });
    }

    private void proceedToLogout() {
        InterfacesUtilities.guardarUsuario(getApplicationContext(), null);
        TransitionUI.destino = LoginUI.class;
        startActivity(new Intent(getApplicationContext(), TransitionUI.class));
        finish();
    }
}
