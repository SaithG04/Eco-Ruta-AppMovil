package com.qromarck.reciperu.Interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.qromarck.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.qromarck.reciperu.DAO.UsuarioDAO;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;
import com.qromarck.reciperu.Utilities.DeviceUtils;
import com.qromarck.reciperu.Utilities.DialogUtilities;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;
import com.qromarck.reciperu.Utilities.NetworkUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RegistroUsuarioUI extends AppCompatActivity {

    // UI Components
    private EditText edtUsuario, edtCorreo, edtContrasena, edtConfirm;
    private FrameLayout loadingLayout;
    private ProgressBar loadingIndicator;

    // Firebase and Google Sign-In components
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1000;
    private static final String TAG = "GoogleSingIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario_ui);

        // Initialize Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase components
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        edtConfirm = findViewById(R.id.edtContrasenaConfirmREG);
        Button registrarCorreo = findViewById(R.id.btnRegCorreo);
        Button registrarGoogle = findViewById(R.id.btnRegGoogle);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Set up listeners for buttons
        registrarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCampos()) {
                    Usuario nuevoUsuario = crearUsuario();
                    String password = edtContrasena.getText().toString();
                    registrarUsuarioOnFireStore(nuevoUsuario, password);
                }
            }
        });

        registrarGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingIndicator();
                SignInWithGoogleAccount();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingIndicator();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideLoadingIndicator();
        TransitionUI.destino = LoginUI.class;
        Log.d("DEBUG", "FROM: " + RegistroUsuarioUI.class.getSimpleName());
        startActivity(new Intent(RegistroUsuarioUI.this, TransitionUI.class));
        finish();
    }

    /**
     * Register a new user on Firestore.
     *
     * @param usuario  User data
     * @param password User password
     */
    private void registrarUsuarioOnFireStore(Usuario usuario, String password) {
        showLoadingIndicator();
        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            if (InterfacesUtilities.isGoogleEmail(usuario.getEmail())) {
                hideLoadingIndicator();
                Toast.makeText(getApplicationContext(), "Este correo pertenece a una cuenta de Google. Intente con otro.", Toast.LENGTH_LONG).show();
            } else {
                mAuth.createUserWithEmailAndPassword(usuario.getEmail(), password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    usuario.setId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                                    UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario);
                                    usuarioDAO.insertOnFireStore(new DataAccessUtilities.OnInsertionListener() {
                                        @Override
                                        public void onInsertionSuccess() {
                                            usuarioDAO.getUserOnFireBase(usuario.getId(), new OnSuccessListener<List<Usuario>>() {
                                                @Override
                                                public void onSuccess(List<Usuario> usuarios) {
                                                    if (!usuarios.isEmpty()) {

                                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                                        enviarCorreoVerificacion(firebaseUser);
                                                        TransitionUI.destino = LoginUI.class;
                                                        Intent intent = new Intent(getApplicationContext(), TransitionUI.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        System.out.println("USUARIO ES NULL");
                                                    }
                                                }
                                            }, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace(System.out);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onInsertionError(String errorMessage) {
                                            System.out.println(errorMessage);
                                        }
                                    });
                                } else {
                                    Log.e("REGISTRO", "NO SE REGISTRO AL USUARIO");
                                    hideLoadingIndicator();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                handleRegistrationError(e);
                            }
                        });
            }
        } else {
            hideLoadingIndicator();
            DialogUtilities.showNoInternetDialog(RegistroUsuarioUI.this);
        }
    }

    /**
     * Handle registration errors.
     *
     * @param e Exception occurred during registration
     */
    private void handleRegistrationError(Exception e) {
        String errorMessage;
        if (Objects.requireNonNull(e.getMessage()).contains("The email address is already in use by another account.")) {
            errorMessage = "¡Ups! Parece que alguien más ya está usando ese email.";
            edtCorreo.setError("");
            edtCorreo.requestFocus();
        } else if (e.getMessage().contains("The email address is badly formatted.")) {
            errorMessage = "¡Ups! Parece que el email que has ingresado no es válido.";
            edtCorreo.setError("");
            edtCorreo.requestFocus();
        } else if (e.getMessage().contains("The given password is invalid. [ Password should be at least 6 characters ]")) {
            edtContrasena.setError("");
            errorMessage = "Tu contraseña debe contener al menos 6 caracteres.";
            edtContrasena.requestFocus();
        } else if (e.getMessage().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")) {
            errorMessage = "Parece que estamos desconectados :(";
        } else {
            errorMessage = "¡Ups! Algo salió mal.";
        }
        hideLoadingIndicator();
        Toast.makeText(RegistroUsuarioUI.this, errorMessage, Toast.LENGTH_LONG).show();
        e.printStackTrace(System.out);
    }

    /**
     * Validate user input fields.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateCampos() {
        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        String password = edtContrasena.getText().toString();
        String confirmPassword = edtConfirm.getText().toString();

        if (fullName.isEmpty()) {
            edtUsuario.setError("Ingrese su nombre completo");
            return false;
        } else if (email.isEmpty()) {
            edtCorreo.setError("Ingrese su correo");
            return false;
        } else if (password.isEmpty()) {
            edtContrasena.setError("Ingrese una clave");
            return false;
        } else if (confirmPassword.isEmpty()) {
            edtContrasena.setError("Ingrese nuevamente su clave");
            return false;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void enviarCorreoVerificacion(FirebaseUser usuario) {
        usuario.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Correo de verificación enviado a " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("RegistroUsuario", "Error al enviar correo de verificación", e);
                        Toast.makeText(getApplicationContext(), "Error al enviar correo de verificación.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Create a new user with the input data.
     *
     * @return new Usuario instance
     */
    @NonNull
    private Usuario crearUsuario() {
        String fullName = edtUsuario.getText().toString();
        String email = edtCorreo.getText().toString();
        String idDevice = DeviceUtils.getAndroidID(getApplicationContext());
        return new Usuario(fullName, email, idDevice);
    }

    /**
     * Start Google Sign-In process.
     */
    private void SignInWithGoogleAccount() {
        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(singInIntent, RC_SIGN_IN);
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
     * Show loading indicator.
     */
    private void showLoadingIndicator() {
        InterfacesUtilities.showLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Hide loading indicator.
     */
    private void hideLoadingIndicator() {
        InterfacesUtilities.hideLoadingIndicator(RegistroUsuarioUI.this, loadingLayout, loadingIndicator);
    }

    /**
     * Authenticate user with Google.
     *
     * @param idToken Google ID token
     */
    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
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
                                // El usuario no existe, es nuevo, así que procede con el registro
                                Log.d(TAG, "signInWithCredential:success");
                                String idDevice = DeviceUtils.getAndroidID(getApplicationContext());
                                Usuario nuevoUsuario = new Usuario(user.getDisplayName(), user.getEmail(), idDevice);
                                nuevoUsuario.setId(user.getUid());
                                nuevoUsuario.setStatus("logged in");

                                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(nuevoUsuario);
                                usuarioDAO.insertOnFireStore(new DataAccessUtilities.OnInsertionListener() {
                                    @Override
                                    public void onInsertionSuccess() {
                                        usuarioDAO.getUserOnFireBase(nuevoUsuario.getId(), new OnSuccessListener<List<Usuario>>() {
                                            @Override
                                            public void onSuccess(List<Usuario> usuarios) {
                                                if (!usuarios.isEmpty()) {
                                                    Usuario usuarioGet = usuarios.get(0);
                                                    InterfacesUtilities.guardarUsuario(getApplicationContext(), usuarioGet);
                                                    TransitionUI.destino = MenuUI.class;
                                                    Intent intent = new Intent(getApplicationContext(), MenuUI.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    Toast.makeText(getApplicationContext(), "¡En hora buena, ahora eres parte de esta familia!.", Toast.LENGTH_LONG).show();
                                                    finish();
                                                } else {
                                                    System.out.println("USUARIO ES NULL");
                                                }
                                            }
                                        }, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace(System.out);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onInsertionError(String errorMessage) {
                                        System.out.println(errorMessage);
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Este correo ya está en uso", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Desconectar al usuario
                                mGoogleSignInClient.signOut(); // Desconectar de Google también
                                hideLoadingIndicator();
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace(System.out);
                        }
                    });
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}