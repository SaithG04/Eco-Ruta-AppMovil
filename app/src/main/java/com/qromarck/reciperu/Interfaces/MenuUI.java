package com.qromarck.reciperu.Interfaces;

import static com.qromarck.reciperu.Interfaces.ReciMapsUI.REQUEST_LOCATION_PERMISSION;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.CommonServiceUtilities;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

public class MenuUI extends AppCompatActivity {
    private Button VerMapa;
    private boolean requestMapPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_ui);

        Usuario userLoggedOnSystem = CommonServiceUtilities.recuperarUsuario(MenuUI.this);

        // Verifica si el objeto Usuario está inicializado
        if (userLoggedOnSystem != null) {
            // Obtiene el nombre de usuario y lo muestra en el TextView
            String nombreUsuario = userLoggedOnSystem.getFull_name();
            TextView txvnombreUSER = findViewById(R.id.txvUSERNAME);
            txvnombreUSER.setText(String.format("Bienvenido, %s", nombreUsuario));
        } else {
            System.out.println("Usuario no disponible");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        VerMapa = findViewById(R.id.btnVerMapa);
        VerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica si tienes los permisos de ubicación
                if (ContextCompat.checkSelfPermission(MenuUI.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Si tienes los permisos, inicia la actividad ReciMapsUI
                    startActivity(new Intent(MenuUI.this, ReciMapsUI.class));
                } else {
                    // Si no tienes los permisos, solicítalos al usuario
                    requestMapPermission = true;
                    ActivityCompat.requestPermissions(MenuUI.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        });

        // Obtén el botón de cerrar sesión
        Button cerrarSesionButton = findViewById(R.id.btnCerrarSesion);

        // Configura el OnClickListener para el botón de cerrar sesión
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestMapPermission) {
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
//                if (requestMapPermission) {
                    // Si la solicitud de permisos es para iniciar la actividad ReciMapsUI
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Si el usuario concede los permisos, inicia la actividad ReciMapsUI
                        startActivity(new Intent(MenuUI.this, ReciMapsUI.class));
                    } else {
                        // Si el usuario deniega los permisos, muestra un mensaje
                        Toast.makeText(MenuUI.this, "Para ver el mapa, necesitas conceder los permisos de ubicación.", Toast.LENGTH_SHORT).show();
                    }
                    requestMapPermission = false; // Restablece la variable a false
//                }
            }
        }
    }

    // Método para manejar el cierre de sesión
    private void cerrarSesion() {
        // Inicia la actividad principal
        Intent intent = new Intent(MenuUI.this, LoginPrincipalUI.class); // Reemplaza `PrincipalUI` con el nombre de tu actividad principal
        Usuario usuario = CommonServiceUtilities.recuperarUsuario(MenuUI.this);
        usuario.setStatus("logged out");
        desloguearUser(usuario);
        CommonServiceUtilities.guardarUsuario(MenuUI.this, null);
        startActivity(intent);
        // Finaliza la actividad actual
        finish();
    }

    private void desloguearUser(Usuario usuario) {
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, MenuUI.this);
        usuarioDAO.updateOnFireStore();
        FirebaseAuth.getInstance().signOut();
    }

}
