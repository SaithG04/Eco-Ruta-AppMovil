package com.qromarck.reciperu.Interfaces;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

import java.util.List;
import java.util.Objects;

public class LoginUI extends AppCompatActivity {

    // Declaración de variables
    private EditText edtCorreo;
    private EditText edtContrasena;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 1000;
    private static final String TAG = "GoogleSingIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar el diseño y bordes transparentes
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar opciones de inicio de sesión de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializar elementos de la interfaz de usuario
        edtCorreo = findViewById(R.id.edtCorreoLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        Button btnRegistrarse = findViewById(R.id.btnRegistrar);
        Button btnLogin = findViewById(R.id.btnLoginLOG);
        ImageView viewRegGoogle = findViewById(R.id.viewRegistrarGoogle);
        TextView restablecer = findViewById(R.id.txvRestablecer);
        TextView reenviar = findViewById(R.id.txvReenviar);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Configurar el botón para abrir la actividad de registro de usuario
        btnRegistrarse.setOnClickListener(view -> {
            TransitionUI.destino = RegistroUsuarioUI.class;
            Log.d("DEBUG", "FROM: " + LoginUI.class.getSimpleName());
            startActivity(new Intent(LoginUI.this, TransitionUI.class));
        });

        // Configurar el botón de inicio de sesión
        btnLogin.setOnClickListener(view -> {
            InterfacesUtilities.hideKeyboard(LoginUI.this);
            String correo = edtCorreo.getText().toString();
            String password = edtContrasena.getText().toString();
            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginUI.this, "¡Ey, faltan datos!", Toast.LENGTH_SHORT).show();
            } else {
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    loginOnFireBase(correo, password); // Método para iniciar sesión en Firebase
                } else {
                    hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(LoginUI.this);
                }
            }
        });

        // Configurar el botón de inicio de sesión con Google
        viewRegGoogle.setOnClickListener(v -> {
            showLoadingIndicator();
            SignInWithGoogleAccount();
        });

        // Configurar el texto para restablecer la contraseña
        restablecer.setOnClickListener(v -> {
            TransitionUI.destino = RestablecerContra.class;
            startActivity(new Intent(LoginUI.this, TransitionUI.class));
        });

        // Configurar el texto para reenviar el correo de verificación
        reenviar.setOnClickListener(view -> {
            showLoadingIndicator();
            String correo = edtCorreo.getText().toString();
            String password = edtContrasena.getText().toString();
            if (correo.isEmpty() || password.isEmpty()) {
                hideLoadingIndicator();
                Toast.makeText(LoginUI.this, "Debes ingresar tu correo y contraseña.", Toast.LENGTH_SHORT).show();
            } else {
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    verificarYReenviarCorreo(correo, password);
                } else {
                    hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(LoginUI.this);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "Google sign in success " + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Log.w(TAG, "Google sign in failed", e);
                }
            } else {
                hideLoadingIndicator();
                Log.d(TAG, "Google sign in failed " + task.getException());
                Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Método para iniciar sesión con una cuenta de Google.
     */
    private void SignInWithGoogleAccount() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        edtCorreo.setText("");
        edtContrasena.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    /**
     * Método para reenviar el correo de verificación.
     */
    private void verificarYReenviarCorreo(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        hideLoadingIndicator();
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Correo de verificación reenviado a " + email, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("ReenviarVerificacion", "Error al reenviar correo de verificación", task1.getException());
                                            Toast.makeText(getApplicationContext(), "Error al reenviar correo de verificación.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        hideLoadingIndicator();
                        Log.e("ReenviarVerificacion", "Error al reautenticar", task.getException());
                        Toast.makeText(getApplicationContext(), "Verifica tus credenciales.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Método para iniciar sesión en Firebase.
     *
     * @param correo   Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    public void loginOnFireBase(String correo, String password) {
        showLoadingIndicator();

        // Crear un usuario con el correo proporcionado
        Usuario userSearched = new Usuario();
        userSearched.setEmail(correo);
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched);

        // Buscar usuario en Firebase
        usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new OnSuccessListener<List<Usuario>>() {
            @Override
            public void onSuccess(List<Usuario> usuarios) {
                if (usuarios.isEmpty()) {
                    hideLoadingIndicator();
                    edtCorreo.requestFocus();
                    Toast.makeText(LoginUI.this, "No hay ninguna cuenta asociada a este correo.", Toast.LENGTH_LONG).show();
                } else {
                    Usuario usuario = usuarios.get(0);
                    if (usuario.getStatus().equals("logged in")) {
                        hideLoadingIndicator();
                        Toast.makeText(LoginUI.this, "Ya hay una sesión iniciada en este dispositivo.", Toast.LENGTH_LONG).show();
                    } else {
                        logIn(usuario, password);
                    }
                }
            }
        }, e -> e.printStackTrace(System.out));
    }

    /**
     * Método para autenticar al usuario con Google.
     *
     * @param idToken Google ID token
     */
    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Usuario userSearched = new Usuario();
                userSearched.setEmail(user.getEmail());
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userSearched);

                // Buscar usuario en Firebase
                usuarioDAO.getUserOnFireBase(userSearched.getEmail(), new OnSuccessListener<List<Usuario>>() {
                    @Override
                    public void onSuccess(List<Usuario> usuarios) {
                        if (usuarios.isEmpty()) {
                            hideLoadingIndicator();
                            Toast.makeText(getApplicationContext(), "Regístrese primero.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            mGoogleSignInClient.signOut();
                        } else {

                            FirebaseMessaging.getInstance().subscribeToTopic("horarios")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                hideLoadingIndicator();
                                                mAuth.signOut();
                                                mGoogleSignInClient.signOut();
                                                Log.w("ERROR", "Suscripción fallida", task.getException());
                                                Toast.makeText(getApplicationContext(), "Ha ocurrido un error inesperado, reintente.", Toast.LENGTH_LONG).show();
                                            }else{
                                                Usuario userL = usuarios.get(0);
                                                userL.setStatus("logged in");
                                                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userL);
                                                usuarioDAO.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
                                                    @Override
                                                    public void onUpdateComplete() {
                                                        //suscribirUser();
                                                        InterfacesUtilities.guardarUsuario(LoginUI.this, userL);
                                                        TransitionUI.destino = userL.getType().equals("conductor") ? ConductorUI.class : MenuUI.class;
                                                        Log.d("DEBUG", "FROM: " + LoginUI.class.getSimpleName());
                                                        hideLoadingIndicator();
                                                        startActivity(new Intent(LoginUI.this, TransitionUI.class));
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onUpdateError(String errorMessage) {
                                                        Log.w("ERROR", errorMessage);
                                                        hideLoadingIndicator();
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                }, e -> e.printStackTrace(System.out));
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Método para iniciar sesión con correo y contraseña.
     *
     * @param usuario  Usuario a iniciar sesión
     * @param password Contraseña del usuario
     */
    private void logIn(Usuario usuario, String password) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(usuario.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> signInMethods = task.getResult().getSignInMethods();
                Log.d("LoginActivity", "SignIn Methods: " + signInMethods);

                if (signInMethods != null && signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                    Toast.makeText(getApplicationContext(), "Este correo está asociado con una cuenta de Google.", Toast.LENGTH_LONG).show();
                    hideLoadingIndicator();
                } else {

                    FirebaseMessaging.getInstance().subscribeToTopic("horarios")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        hideLoadingIndicator();
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        Log.w("ERROR", "Suscripción fallida", task.getException());
                                        Toast.makeText(getApplicationContext(), "Ha ocurrido un error inesperado, reintente.", Toast.LENGTH_LONG).show();
                                    }else{
                                        mAuth.signInWithEmailAndPassword(usuario.getEmail(), password).addOnCompleteListener(authTask -> {
                                            if (authTask.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                boolean test = usuario.getEmail().equals("prueba@gmail.com");
                                                if ((user != null && user.isEmailVerified()) || test ) {
                                                    usuario.setStatus(test ? "logged out" : "logged in");
                                                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario);
                                                    usuarioDAO.updateOnFireStore(new DataAccessUtilities.OnUpdateListener() {
                                                        @Override
                                                        public void onUpdateComplete() {
                                                            InterfacesUtilities.guardarUsuario(LoginUI.this, usuario);
                                                            TransitionUI.destino = usuario.getType().equals("conductor") ? ConductorUI.class : MenuUI.class;
                                                            Log.d("DEBUG", "FROM: " + LoginUI.class.getSimpleName());
                                                            startActivity(new Intent(LoginUI.this, TransitionUI.class));
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onUpdateError(String errorMessage) {
                                                            Log.w("ERROR", errorMessage);
                                                            hideLoadingIndicator();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(LoginUI.this, "Revisa tu correo para verificar tu cuenta.", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                    hideLoadingIndicator();
                                                }
                                            } else {
                                                handleLoginFailure(authTask.getException());
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                handleLoginFailure(e);
                                            }
                                        });
                                    }
                                }
                            });
                }
            } else {
                Log.w("LoginActivity", "Error fetching sign-in methods for user", task.getException());
                Toast.makeText(getApplicationContext(), "Ha ocurrido un error, pruebe de nuevo.", Toast.LENGTH_SHORT).show();
                hideLoadingIndicator();
            }
        });
    }

    /**
     * Manejar errores de inicio de sesión.
     *
     * @param e Excepción de error
     */
    private void handleLoginFailure(Exception e) {
        e.printStackTrace(System.out);
        String errorMessage;
        int duration = Toast.LENGTH_SHORT;

        if (Objects.requireNonNull(e.getMessage()).contains("The email address is badly formatted.")) {
            errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
            edtCorreo.requestFocus();
        } else if (e.getMessage().contains("The supplied auth credential is incorrect, malformed or has expired.") || e.getMessage().contains("The password is invalid or the user does not have a password.")) {
            errorMessage = "Verifique nuevamente su contraseña por favor.";
            edtContrasena.setText("");
            edtContrasena.requestFocus();
        } else if (e.getMessage().contains("We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]")) {
            errorMessage = "Se ha bloqueado temporalmente el acceso a tu cuenta debido a demasiados intentos de inicio de sesión fallidos. Por favor, restablece tu contraseña o inténtalo más tarde.";
            duration = Toast.LENGTH_LONG;
        } else if (e.getMessage().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
            errorMessage = "Verifique su conexión a internet.";
            edtCorreo.setText("");
            edtContrasena.setText("");
            edtCorreo.requestFocus();
        } else {
            errorMessage = "Lo sentimos, ha ocurrido un error.";
        }
        Toast.makeText(LoginUI.this, errorMessage, duration).show();
        hideLoadingIndicator();
    }

    /**
     * Método para mostrar el indicador de carga.
     */
    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(LoginUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Método para ocultar el indicador de carga.
     */
    private void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(LoginUI.this, loadingLayout, loadingIndicator);
    }
}
