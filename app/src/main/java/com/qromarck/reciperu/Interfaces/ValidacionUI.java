package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

import java.util.Objects;

public class ValidacionUI extends AppCompatActivity {

    private EditText edtContrasena;

    private FirebaseAuth mAuth;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_validacion_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnLogin = findViewById(R.id.btnLoginLOG);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingIndicator();
                String password = edtContrasena.getText().toString();
                if (password.isEmpty() || password.length() < 6) {
                    Toast.makeText(ValidacionUI.this, "Ingrese una contraseña válida.", Toast.LENGTH_SHORT).show();
                    hideLoadingIndicator();
                } else {
                    String correo = InterfacesUtilities.recuperarUsuario(ValidacionUI.this).getEmail();
                    logIn(correo, password);
                }
            }
        });

    }

    private void logIn(String correo, String password) {
        mAuth.signInWithEmailAndPassword(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    TransitionUI.destino = MenuUI.class;
                    Log.d("DEBUG", "FROM: " + ValidacionUI.class.getSimpleName());
                    startActivity(new Intent(ValidacionUI.this, TransitionUI.class)); // Abrir actividad del menú
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace(System.out);
                String errorMessage = null;
                int duration = Toast.LENGTH_SHORT;

                // Manejar diferentes tipos de errores de inicio de sesión
                if (Objects.requireNonNull(e.getMessage()).contains("The supplied auth credential is incorrect, malformed or has expired.")) {
                    errorMessage = "Verifique nuevamente su contraseña por favor.";
                    edtContrasena.setText("");
                    edtContrasena.requestFocus();
                } else if (e.getMessage().contains("We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")) {
                    errorMessage = "Se ha bloqueado temporalmente el acceso a tu cuenta debido a demasiados intentos de inicio de sesión fallidos. Por favor, restablece tu contraseña o inténtalo más tarde.";
                    duration = Toast.LENGTH_LONG;
                } else if (e.getMessage().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
                    errorMessage = "Verifique su conexión a internet.";
                    edtContrasena.setText("");
                } else {
                    errorMessage = "Lo sentimos, ha ocurrido un error.";
                }
                Toast.makeText(ValidacionUI.this, errorMessage, duration).show();
                hideLoadingIndicator();
            }
        });
    }

    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(ValidacionUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    public void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(ValidacionUI.this, loadingLayout, loadingIndicator);
    }
}