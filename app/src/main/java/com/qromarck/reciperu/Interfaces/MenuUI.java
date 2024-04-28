package com.qromarck.reciperu.Interfaces;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.MenuUIManager;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.SecurityUtilities;

import java.io.Serializable;

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

    public TextView getReci() {
        return reci;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_ui);

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
                // Verifica si tienes los permisos de ubicación
                if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Si tienes los permisos, inicia la actividad ReciMapsUI
                    exit = false;
                    TransitionUI.destino = ReciMapsUI.class;
                    startActivity(new Intent(MenuUI.this, TransitionUI.class));
                } else {
                    // Si no tienes los permisos, solicítalos al usuario
                    ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    hideLoadingIndicator();
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
                String[] texto = contents.split(" ");
                if (texto [0].equals(SecurityUtilities.CODEQR)) {
                    int puntosObtenidos = Integer.parseInt(texto[1]);
                    sumarpuntos(puntosObtenidos);
                }else{
                    Toast.makeText(getApplicationContext(),"QR INVALIDO CTM (QUE QUERIAS MARICON?)",Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                }
            }else{
                Toast.makeText(getApplicationContext(),"QR INVALIDO CTM (QUE QUERIAS MARICON?)",Toast.LENGTH_SHORT).show();
                hideLoadingIndicator();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        typeChange = "";
//        hideLoadingIndicator();
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
                // Si el usuario concede los permisos, inicia la actividad ReciMapsUI
                startActivity(new Intent(MenuUI.this, ReciMapsUI.class));

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
            }
        });
        // Mostrar el diálogo
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
        //Creamos usuario DAO
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(recuperarUsuario, MenuUI.this);
        typeChange = "sumaptos";
        //Actualiza en firestore
        usuarioDAO.updateOnFireStore();

    }

}
