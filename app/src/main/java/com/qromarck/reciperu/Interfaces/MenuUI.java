package com.qromarck.reciperu.Interfaces;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qromarck.reciperu.DAO.DAOImplements.QrDAOImpl;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.QrDAO;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.MenuUIManager;
import com.qromarck.reciperu.Entity.QR;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MenuUI extends AppCompatActivity implements Serializable {

    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    public static String typeChange = "";

    private static boolean exit;

    //QR
    private TextView reci;
    private Button scan_btn;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ImageView btnShop;

    public TextView getReci() {
        return reci;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_ui);

        inicializarUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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
                        if (!isGPSEnabled()) {
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

        scan_btn = findViewById(R.id.btnCamara);

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
                // Permiso de la cámara concedido, iniciar el escaneo
                startBarcodeScanning();
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

        btnShop = findViewById(R.id.imgShop);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionUI.destino = ReciShop.class;
                startActivity(new Intent(MenuUI.this, TransitionUI.class));
            }
        });
    }

    private void startBarcodeScanning() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(MenuUI.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Escanear un QR");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                QR qr = new QR();
                String sede = "26 de Octubre";
                qr.setSede(sede);
                QrDAO qrDAO = new QrDAOImpl(qr, MenuUI.this);
                qrDAO.getQROnFireBase(qr.getSede(), new QrDAOImpl.OnQrRetrievedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onQrRetrieved(QR qr) {
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
                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                try {
                                                    boolean isPosible = false;

                                                    Usuario usuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
                                                    Timestamp lastScanDate = usuario.getLast_scan_date();
                                                    Timestamp registroDate = usuario.getRegistro_date();

                                                    if (lastScanDate == null || registroDate == null) {
                                                        Toast.makeText(getApplicationContext(), "Algo salió mal. Cierre y inicie nuevamente la sesión en su cuenta.", Toast.LENGTH_SHORT).show();
                                                        hideLoadingIndicator();
                                                    } else {
                                                        Object timeGet = dataSnapshot.child("time").getValue();
                                                        if (timeGet instanceof Long) {
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
                                                        } else {
                                                            System.out.println("NO SE QUE CHUCHA");
                                                            // Manejo para el caso en que no se obtenga un valor válido de la base de datos
                                                        }

//                                                    Object timeGet = dataSnapshot.child("time").getValue();
//                                                    System.out.println(timeGet);
//                                                    Date date = new Date((Long) timeGet);
//                                                    Timestamp actualDate = new Timestamp(date);
//                                                    if (InterfacesUtilities.compareDatesWithoutHMS(actualDate, registroDate)) {
//                                                        if (lastScanDate.equals(registroDate)) {
//                                                            isPosible = true;
//                                                        }
//                                                    } else {
//                                                        if (!InterfacesUtilities.compareDatesWithoutHMS(actualDate, lastScanDate)) {
//                                                            isPosible = true;
//                                                        }
//                                                    }
//
//                                                    if (isPosible) {
//                                                        sumarpuntos(qr.getPoints_value());
//                                                    } else {
//                                                        Toast.makeText(getApplicationContext(), "Podrás acumular más puntos mañana.", Toast.LENGTH_SHORT).show();
//                                                        hideLoadingIndicator();
//                                                    }
//                                                    }

                                                    }

//                                                    dataSnapshot.child("time").getValue();

//                                                    Timestamp registrationDateTimeObj = (Timestamp) dataSnapshot.child("time").getValue();
//                                                        if (registrationDateTimeObj instanceof Long) {
//                                                        long registrationDateTimeMillis = (long) registrationDateTimeObj;
//                                                        Date registrationDate = new Date(registrationDateTimeMillis);
//                                                        Timestamp actualDate = (Timestamp) ;


                                                } catch (Exception exception) {
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
                });

            } else {
                hideLoadingIndicator();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        inicializarUser();
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
                if (!isGPSEnabled()) {
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
                cerrarSesion();
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
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, MenuUI.this);
        usuarioDAO.updateOnFireStore();
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
        Usuario recuperarUsuario = InterfacesUtilities.recuperarUsuario(getApplicationContext());
        //Recuperar ptos usuarios
        int ptosactuales = recuperarUsuario.getPuntos();
        ptosactuales += puntos;
        //Actualizar ptos en usuario
        recuperarUsuario.setPuntos(ptosactuales);
        recuperarUsuario.setLast_scan_date(null);
        //Creamos usuario DAO
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(recuperarUsuario, MenuUI.this);
        typeChange = "sumaptos";
        //Actualiza en firestore
        usuarioDAO.updateOnFireStore();

    }

    private void inicializarUser() {
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
