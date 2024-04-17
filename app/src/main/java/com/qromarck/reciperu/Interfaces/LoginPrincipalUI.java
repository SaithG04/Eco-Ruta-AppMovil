package com.qromarck.reciperu.Interfaces;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import android.content.Intent;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;

import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.ExampleCustomCredential;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.*;

import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Clase que representa la interfaz de usuario principal de la aplicación.
 */
public class LoginPrincipalUI extends AppCompatActivity {

    // Declaración de variables

    /**
     * Campo de texto para el correo electrónico.
     */
    private EditText edtCorreo;
    /**
     * Campo de texto para la contraseña.
     */
    private EditText edtContrasena;
    /**
     * Diseño de la interfaz de usuario para mostrar el indicador de carga.
     */
    private FrameLayout loadingLayout;
    /**
     * Indicador de carga.
     */
    private ProgressBar loadingIndicator;
    /**
     * Instancia de FirebaseAuth para la autenticación de Firebase.
     */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el diseño de la actividad y configurar bordes transparentes
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar elementos de la interfaz de usuario
        Button btnRegistrarse = findViewById(R.id.btnRegistrar);
        Button btnLogin = findViewById(R.id.btnLoginLOG);
        edtCorreo = findViewById(R.id.edtCorreoLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Inicializar FirebaseAuth para la autenticación de Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configurar el botón para abrir la actividad de registro de usuario
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPrincipalUI.this, RegistroUsuarioUI.class);
                startActivity(intent);
            }
        });

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = edtCorreo.getText().toString();
                String password = edtContrasena.getText().toString();
                if (correo.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginPrincipalUI.this, "¡Ey, faltan datos!", Toast.LENGTH_SHORT).show();
                } else {
                    loginOnFireBase(correo, password); // Método para iniciar sesión en Firebase

                }
            }
        });
    }

    /**
     * Método para iniciar sesión en Firebase.
     *
     * @param correo   Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    private void loginOnFireBase(String correo, String password) {
        showLoadingIndicator(); // Mostrar indicador de carga

        // Crear un usuario con el correo proporcionado
        Usuario userSearched = new Usuario();
        userSearched.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched, LoginPrincipalUI.this);

        // Buscar usuario en Firebase
        usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new UsuarioDAOImpl.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(Usuario usuario) {
                if (usuario == null) { // Si no se encuentra el usuario
                    hideLoadingIndicator(); // Ocultar indicador de carga
                    Toast.makeText(LoginPrincipalUI.this, "No hay ninguna cuenta asociada a este correo.", Toast.LENGTH_LONG).show();
                } else {
                    // Iniciar sesión con el correo y la contraseña proporcionados
                    mAuth.signInWithEmailAndPassword(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = CommonServiceUtilities.getSystemEditor(LoginPrincipalUI.this);
                                editor.putString("email", correo);
                                editor.putString("fullName", usuario.getFull_name());
                                editor.apply();
                                DataAccessUtilities.usuario = usuario; // Establecer el usuario en DataAccessUtilities
                                finish();
                                startActivity(new Intent(LoginPrincipalUI.this, MenuUI.class)); // Abrir actividad del menú
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace(System.out);
                            String errorMessage = null;
                            int duration = Toast.LENGTH_SHORT;

                            String errorCode = ((FirebaseAuthException) e).getErrorCode(); // Suponiendo que estás utilizando FirebaseAuthException
                            System.out.println(errorCode);

                            // Manejar diferentes tipos de errores de inicio de sesión
                            if (e.getMessage().contains("The email address is badly formatted.")) {
                                errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
                                edtCorreo.requestFocus();
                            } else if (e.getMessage().contains("The supplied auth credential is incorrect, malformed or has expired.")) {
                                errorMessage = "Verifique nuevamente su contraseña por favor.";
                                edtContrasena.setText("");
                                edtContrasena.requestFocus();
                            } else if (e.getMessage().contains("We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")) {
                                errorMessage = "Se ha bloqueado temporalmente el acceso a tu cuenta debido a demasiados intentos de inicio de sesión fallidos. Por favor, restablece tu contraseña o inténtalo más tarde.";
                                duration = Toast.LENGTH_LONG;
                            } else {
                                errorMessage = "Lo sentimos, ha ocurrido un error.";
                            }
                            Toast.makeText(LoginPrincipalUI.this, errorMessage, duration).show();
                            hideLoadingIndicator();
                        }
                    });
                }
            }
        });

    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        CommonServiceUtilities.showLoadingIndicator(LoginPrincipalUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    private void hideLoadingIndicator() {
        CommonServiceUtilities.hideLoadingIndicator(LoginPrincipalUI.this, loadingLayout, loadingIndicator);
    }

    private void log() {
        // Define la URL de donde quieres obtener el JSON
        String url = "https://reciperu2024.000webhostapp.com/.well-known/assetlinks.json";

        // Crea una solicitud de cadena utilizando Volley
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String requestJson) {
                        // CredentialManager.
                        CredentialManager credentialManager = CredentialManager.create(getApplicationContext());

                        // Retrieves the user's saved password for your app from their
                        // password provider.
                        GetPasswordOption getPasswordOption = new GetPasswordOption();

                        // Get passkey from the user's public key credential provider.
                        GetPublicKeyCredentialOption getPublicKeyCredentialOption =
                                new GetPublicKeyCredentialOption(requestJson);


                        GetCredentialRequest getCredRequest = new GetCredentialRequest.Builder()
                                .addCredentialOption(getPasswordOption)
                                .addCredentialOption(getPublicKeyCredentialOption)
                                .build();

                        credentialManager.getCredentialAsync(
                                // Use activity based context to avoid undefined
                                // system UI launching behavior
                                LoginPrincipalUI.this,
                                getCredRequest,
                                null,
                                null,
                                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                                    @Override
                                    public void onResult(GetCredentialResponse result) {
                                        handleSignIn(result);
                                    }

                                    @Override
                                    public void onError(GetCredentialException e) {
                                        handleFailure(e);
                                    }
                                }
                        );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Maneja el error de la solicitud
            }
        });

        // Agrega la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();
        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
            // Share responseJson i.e. a GetCredentialResponse on your server to validate and authenticate
        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            // Use id and password to send to your server to validate and authenticate
        }
//        else if (credential instanceof CustomCredential) {
//            if (ExampleCustomCredential.TYPE.equals(credential.getType())) {
//                try {
//                    ExampleCustomCredential customCred = ExampleCustomCredential.createFrom(customCredential.getData());
//                    // Extract the required credentials and complete the
//                    // authentication as per the federated sign in or any external
//                    // sign in library flow
//                } catch (ExampleCustomCredential.ExampleCustomCredentialParsingException e) {
//                    // Unlikely to happen. If it does, you likely need to update the
//                    // dependency version of your external sign-in library.
//                    Log.e(TAG, "Failed to parse an ExampleCustomCredential", e);
//                }
//            } else {
//                // Catch any unrecognized custom credential type here.
//                Log.e(TAG, "Unexpected type of credential");
//            }
//        }
        else {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential");
        }
    }

    private void handleFailure(GetCredentialException e) {
        // Maneja el fallo de la obtención de credenciales
    }

}