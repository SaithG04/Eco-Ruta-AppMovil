package com.example.reciperu;


import android.os.Bundle;

import android.text.Editable;
import android.widget.Button;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;

import android.content.Intent;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.Interfaces.RegistroUI;
import com.example.reciperu.Utilities.CommonServiceUtilities;
import com.example.reciperu.Utilities.DataAccessUtilities;


import android.widget.EditText;
import android.widget.Toast;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

import java.util.ArrayList;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private Button btnRegistrarse;

    private EditText edtusuario, edtContrasena;
    private Button btnLogin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        //Referenciar boton Regisrarse
        btnRegistrarse = findViewById(R.id.btnRegistrar);
        edtusuario = findViewById(R.id.edtUsuarioLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        btnLogin = findViewById(R.id.btnLoginLOG);

        // Configurar el listener del botón
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un Intent para iniciar la nueva actividad
                Intent intent = new Intent(MainActivity.this, RegistroUI.class);
                edtusuario.setText("");
                edtContrasena.setText("");

                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtusuario.getText().toString();
                String password = edtContrasena.getText().toString();
                Usuario userFind = new Usuario();
                userFind.setNombre(username);
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(userFind, getApplicationContext());
                usuarioDAO.getByUsername(new DataAccessUtilities.OnDataRetrievedOneListener<Usuario>() {
                    @Override
                    public void onDataRetrieved(Usuario userReceived) {
                        if (userReceived != null) {
                            byte[] inputHashedPassword = CommonServiceUtilities.hashPassword(password, userReceived.getSalt());
                            if (!MessageDigest.isEqual(userReceived.getHashedPassword(), inputHashedPassword)) {
                                // Mostrar mensaje de error si la contraseña es incorrecta
                                Toast.makeText(getApplicationContext(), "Contraseña incorrecta. Verifique nuevamente.", Toast.LENGTH_SHORT).show();
                                edtContrasena.setText("");
                                edtusuario.requestFocus();
//                                ++intentos;
                                return; // Finalizar el método si la contraseña es incorrecta
                            }
                            // Crear un Intent para iniciar la nueva actividad
                            Intent intent = new Intent(MainActivity.this, ReciMaps.class);
                            edtusuario.setText("");
                            edtContrasena.setText("");

                            // Iniciar la nueva actividad
                            startActivity(intent);
                        } else {
                            // Si la lista de usuarios está vacía, mostrar un mensaje indicando esto
                            Toast.makeText(getApplicationContext(), "No se encontró el usuario.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Manejar el error en caso de problemas con la solicitud
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }


}