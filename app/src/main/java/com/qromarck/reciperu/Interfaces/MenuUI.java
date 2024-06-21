package com.qromarck.reciperu.Interfaces;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.entity.UrlEncodedFormEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qromarck.reciperu.DAO.DAOImplements.QrDAOImpl;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.QrDAO;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.MenuUIManager;
import com.qromarck.reciperu.Entity.EcoNotification;
import com.qromarck.reciperu.Entity.QR;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.*;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.view.MenuItem;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MenuUI extends AppCompatActivity implements Serializable {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    public static String typeChange = "";

    private static boolean exit;
    private TextView reci;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    public TextView getReci() {
        return reci;
    }

    //CONSEJOS

    private HorizontalScrollView horizontalScrollView;
    private Handler handler = new Handler();
    private int scrollMax;
    private int scrollPos = 0;
    private int[] scrollPositions = {0,1600,3100,4800}; // Ejemplo de posiciones específicas
    private int currentPosIndex = 0;
    private static final int STOP_DURATION = 2000; // Tiempo en milisegundos para detenerse en cada posición
    //CONSEJOS
    private static final int SCROLL_DELAY = 10; // Tiempo en milisegundos entre cada scroll
    private static final int SCROLL_DELAY_FAST = 5; // Tiempo en milisegundos entre cada scroll rápido en reversa
    private static final int SCROLL_INCREMENT = 20; // Cantidad de píxeles para desplazarse en cada iteración



    //MENU
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;


    //IMAGEN PROFILE

    private ImageView imgUser,btnSelectImage,imgloading;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;
    private String UPLOAD_URL = "https://reciperu2024.000webhostapp.com/upload.php"; // URL de tu script PHP en el servidor
    private String KEY_IMAGE = "foto";
    private String KEY_NAME = "nombre"; // Ajusta el nombre del parámetro según tu script PHP



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_ui);

        //SUBIR FOTO
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo imagen...");

        btnSelectImage = findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(); // Llama primero a selectImage para obtener la imagen desde la galería
            }
        });

        //LISTAR FOTO

        imgUser = findViewById(R.id.userImageView);

        imgloading = findViewById(R.id.loading_profilegif);

        Usuario nombreUsuarioLogged = InterfacesUtilities.recuperarUsuario(MenuUI.this);
        String nameuserLogged = nombreUsuarioLogged.getFull_name().toString();

        // Aquí deberías tener un método o evento que obtenga el nombre de usuario
        String nombre = nameuserLogged; // Nombre de usuario para la prueba

        // Llamar a AsyncTask para obtener la imagen del servidor
        new GetImageTask().execute(nombre);



        //CONSEJOS
        horizontalScrollView = findViewById(R.id.horizontalScrollView);

        horizontalScrollView.post(() -> {
            scrollMax = horizontalScrollView.getChildAt(0).getMeasuredWidth() - getWindowManager().getDefaultDisplay().getWidth();
            autoScroll();
        });

        //MENU
        drawerLayout = findViewById(R.id.drawer_layout);
        btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Ensure you are using the correct ID
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    // Handle navigation view item clicks here.
                    int id = item.getItemId();

                    if (id == R.id.nav_direccion) {

                    } else if (id == R.id.nav_rutas) {
                        // Inicia la nueva actividad ChangePasswordUI

                        Intent intent = new Intent(MenuUI.this, RutasUI.class);
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START); // Cerrar el menú
                        return true;
                    }else if (id == R.id.nav_cambiarcontra) {
                        // Inicia la nueva actividad ChangePasswordUI

                        Intent intent = new Intent(MenuUI.this, RestablecerContraMenuUser.class);
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START); // Cerrar el menú
                        return true;
                    }

                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
        } else {
            Log.e("MenuUI", "NavigationView is null");
        }



        inicializarUsuario();
        //Falta validar para cuando se desactivan de un canal en específico
        if(NotificationUtilities.areNotificationsEnabled(getApplicationContext())){
            showInitialsNotifications();
        }else{
            DialogUtilities.showNotificationSettingsDialog(MenuUI.this);
        }

        Button verMapa = findViewById(R.id.btnVerMapa);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        MenuUIManager.getInstance().setMenuUI(this);
        exit = true;
        verMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingIndicator();
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    // Verifica si tienes los permisos de ubicación
                    if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Verificar si el GPS está habilitado
                        if (isGPSEnabled()) {
                            hideLoadingIndicator();
                            // El GPS está apagado, mostrar un diálogo para pedir al usuario que lo active
                            showEnableGPSDialog();
                        } else {
                            // El GPS está habilitado, realizar la acción deseada
                            exit = false;
                            TransitionUI.destino = ReciMapsUI.class;
                            Log.d("DEBUG", "FROM: " + MenuUI.class.getSimpleName());
                            startActivity(new Intent(MenuUI.this, TransitionUI.class));
                        }
                    } else {
                        // Si no tienes los permisos, solicítalos al usuario
                        ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                        hideLoadingIndicator();
                    }
                } else {
                    hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(MenuUI.this);
                }
            }
        });

        // Obtén el botón de cerrar sesión
        Button cerrarSesionButton = findViewById(R.id.btnCerrarSesion);

        // Configura el OnClickListener para el botón de cerrar sesión
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        //SCANNER QR

        Button scan_btn = findViewById(R.id.btnCamara);

        scan_btn.setOnClickListener(view -> {
            showLoadingIndicator();
            // Verificar y solicitar permiso de la cámara si no está concedido
            if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                hideLoadingIndicator();
                ActivityCompat.requestPermissions(MenuUI.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    startBarcodeScanning();
                } else {
                    hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(MenuUI.this);
                }

            }
        });
//        PROHIBIDO BORRAR O TE BORRO YO
//        Button add = findViewById(R.id.add);
//        add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String codeqr = SecurityUtilities.CODEQR;
//                int salt = InterfacesUtilities.generateSalt();
//                int hashPassword = InterfacesUtilities.hashPassword(codeqr, salt);
//                QR qr = new QR("SgRzNBy3gm2CvR7hHSif", hashPassword, salt, "26 de Octubre");
//                QrDAO qrDAO = new QrDAOImpl(qr, MenuUI.this);
//                qrDAO.insertOnFireStore();
//            }
//        });


        //TIENDA RECISHOP

        ImageView btnShop = findViewById(R.id.imgShop);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionUI.destino = ReciShop.class;
                startActivity(new Intent(MenuUI.this, TransitionUI.class));
            }
        });


    }

    //FOTO PERFIL

    //LISTAR FOTO

    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar el GIF de carga en el ImageView
            Glide.with(imgloading.getContext())
                    .asGif()
                    .load(R.drawable.loading_profile) // Reemplaza con el nombre de tu archivo GIF de carga
                    .into(imgloading);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String nombre = params[0];
            String serverUrl = "https://reciperu2024.000webhostapp.com/obtener_imagen.php"; // URL de tu script PHP

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Enviar el nombre de usuario al servidor
                String postData = "nombre=" + nombre;
                connection.getOutputStream().write(postData.getBytes());

                // Obtener la respuesta del servidor (la imagen)
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                connection.disconnect();

                return bitmap;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (result != null) {
                // Mostrar la imagen en el ImageView
                imgUser.setImageBitmap(result);
                imgloading.setImageBitmap(null);
            } else {
                // Manejar el caso donde no se pudo obtener la imagen
                // Puedes mostrar una imagen por defecto o un mensaje de error
                Toast.makeText(MenuUI.this, "Error al obtener la imagen / Suba foto de perfil", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //SUBIR FOTO AL SERVER
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione Imagen"), 1);
    }

    // Método para subir la imagen al servidor
    private void uploadImage() {
        if (bitmap == null) {
            Toast.makeText(MenuUI.this, "Bitmap es nulo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Convierte la imagen a cadena base64
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
                        String nameuserLogged = nombreUsuarioLogged.getFull_name().toString();

                        // Aquí deberías tener un método o evento que obtenga el nombre de usuario
                        String nombre = nameuserLogged; // Nombre de usuario para la prueba

                        // Llamar a AsyncTask para obtener la imagen del servidor
                        new GetImageTask().execute(nombre);
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
                //Obtehner nonbre de usuario logeado
                Usuario usuarionombre = InterfacesUtilities.recuperarUsuario(MenuUI.this);
                String usuarionombrestring = usuarionombre.getFull_name().toString();

                params.put(KEY_NAME, usuarionombrestring); // Nombre de ejemplo, ajusta según sea necesario
                params.put(KEY_IMAGE, imageString);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("SE ESTA EJECUTANDO EL ON ACTIVITYRESULT");

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                if (bitmap != null) {
                    uploadImage(); // Llama a uploadImage directamente después de obtener la imagen
                } else {
                    Toast.makeText(MenuUI.this, "Error al obtener la imagen", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Manejo del resultado del escaneo QR
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
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
                                                // Manejar errores de lectura de datos
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

            } else {
                hideLoadingIndicator();
            }
        }
    }

    //CONSEJOS
    private void autoScroll() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verifica si se ha alcanzado el final del desplazamiento
                if (scrollPos >= scrollMax) {
                    // Agrega una pausa al final
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Inicia el desplazamiento en reversa
                            autoScrollReverse();
                        }
                    }, STOP_DURATION); // Pausa antes de iniciar el desplazamiento en reversa
                } else {
                    // Verifica si se ha alcanzado una posición específica
                    if (currentPosIndex < scrollPositions.length && scrollPos >= scrollPositions[currentPosIndex]) {
                        currentPosIndex++;
                        handler.postDelayed(this, STOP_DURATION); // Detiene en la posición específica
                        return;
                    }
                    // Desplaza gradualmente
                    if (currentPosIndex < scrollPositions.length) {
                        int nextPos = scrollPositions[currentPosIndex];
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
                // Verifica si se ha alcanzado el principio del desplazamiento
                if (scrollPos <= 0) {
                    // Inicia el desplazamiento hacia adelante
                    currentPosIndex = 0; // Reinicia el índice de las posiciones específicas
                    autoScroll();
                } else {
                    scrollPos -= SCROLL_INCREMENT; // Desplazamiento rápido hacia atrás
                    horizontalScrollView.scrollTo(scrollPos, 0);
                    handler.postDelayed(this, SCROLL_DELAY_FAST);
                }
            }
        }, SCROLL_DELAY_FAST);
    }


    //NOTIFICACION

    @Override
    protected void onResume() {
        super.onResume();
        inicializarUsuario();
    }

    private void showInitialsNotifications() {

        //Se crea el canal
        String notificationChannelID = EcoNotification.createNotificationChannel(getApplicationContext(), 1, "Menu", "Description");
        //Notificación 1
        NotificationUtilities.showNotification(getApplicationContext(), 1, notificationChannelID,"¡Bienvenido de nuevo!", "¡Gracias por utilizar nuestra aplicación!");
        //Notificación 2
        NotificationUtilities.showNotification(getApplicationContext(), 2, notificationChannelID,"¡Recuerda botar tu basura!", "Ayuda a mantener limpio el ambiente.");

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
        typeChange = "";
        if (exit) {
            finishAffinity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            hideLoadingIndicator();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Verificar si el GPS está habilitado
                if (isGPSEnabled()) {
                    hideLoadingIndicator();
                    // El GPS está apagado, mostrar un diálogo para pedir al usuario que lo active
                    showEnableGPSDialog();
                } else {
                    // El GPS está habilitado, realizar la acción deseada
                    TransitionUI.destino = ReciMapsUI.class;
                    Log.d("DEBUG", "FROM: " + MenuUI.class.getSimpleName());
                    startActivity(new Intent(MenuUI.this, TransitionUI.class));
                }
            } else {
                // Si el usuario deniega los permisos, muestra un mensaje
                Toast.makeText(MenuUI.this, "Para ver el mapa, necesitas conceder los permisos de ubicación.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para mostrar el diálogo de confirmación para cerrar sesión
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción al confirmar cerrar sesión
                typeChange = "deslogueo";
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
        // Mostrar el diálogo
        builder.show();
    }

    // Método para verificar si el GPS está habilitado
    private boolean isGPSEnabled() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE) == null || !((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Método para mostrar un diálogo pidiendo al usuario que active el GPS
    private void showEnableGPSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Desactivado");
        builder.setMessage("Para continuar, active el GPS en su dispositivo.");
        builder.setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Abre la configuración de ubicación del dispositivo para que el usuario pueda habilitar el GPS
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
        builder.setCancelable(false); // Evita que el diálogo se cierre al tocar fuera de él
        builder.show();
    }

    // Método para manejar el cierre de sesión
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
                Log.d("DEBUG", "FROM: " + UsuarioDAOImpl.class.getSimpleName());
                startActivity(new Intent(getApplicationContext(), TransitionUI.class));
                // Finaliza la actividad actual
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

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    public void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(MenuUI.this, loadingLayout, loadingIndicator);
    }

    public void sumarpuntos(int puntos) {
        //Obtener usuario logeado en sistema en general
        Usuario systemUser = InterfacesUtilities.recuperarUsuario(getApplicationContext());
        //Recuperar ptos usuarios
        int ptosactuales = systemUser.getPuntos();
        ptosactuales += puntos;
        //Actualizar ptos en usuario
        systemUser.setPuntos(ptosactuales);
        systemUser.setLast_scan_date(null);
        //Creamos usuario DAO
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(systemUser);
        typeChange = "sumaptos";
        //Actualiza en firestore
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

            }
        });

    }

    private void inicializarUsuario() {
        Usuario userLoggedOnSystem = InterfacesUtilities.recuperarUsuario(MenuUI.this);

        // Verifica si el objeto Usuario está inicializado
        if (userLoggedOnSystem != null) {
            // Obtiene el nombre de usuario y lo muestra en el TextView
            String nombreUsuario = userLoggedOnSystem.getFull_name();
            TextView txvnombreUSER = findViewById(R.id.txvUSERNAME);
            txvnombreUSER.setText(String.format("Bienvenido, %s", nombreUsuario));
            // CAMBIOS
            // Obtiene los puntos del usuario y los coloca en el texView
            int recipoints = userLoggedOnSystem.getPuntos();
            reci = findViewById(R.id.txvReciPoints);
            reci.setText(String.valueOf(recipoints));
        } else {
            System.out.println("Usuario no disponible");
        }
    }

    private void QRError() {
        Toast.makeText(getApplicationContext(), "QR INVALIDO", Toast.LENGTH_SHORT).show();
        hideLoadingIndicator();
    }

}
