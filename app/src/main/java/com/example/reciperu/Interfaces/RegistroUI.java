package com.example.reciperu.Interfaces;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.R;
import com.example.reciperu.Utilities.CommonServiceUtilities;
import com.example.reciperu.Utilities.DataAccessUtilities;

public class RegistroUI extends AppCompatActivity {

    private final CommonServiceUtilities csu = new CommonServiceUtilities();

    private EditText edtUsuario, edtCorreo, edtContrasena;
    private Button registrarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_ui);

        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        registrarButton = findViewById(R.id.btnREG);

        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {


        String nombre = edtUsuario.getText().toString();
        String correo = edtCorreo.getText().toString();
        String contrasena = edtContrasena.getText().toString();


        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show();
        } else if (correo.isEmpty()) {
            Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show();
        } else if (contrasena.isEmpty()) {
            Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
        } else {

            Usuario usuarioFind = new Usuario();
            usuarioFind.setUsuario(nombre);
            usuarioFind.setCorreo(correo);
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuarioFind, this.getApplicationContext());
            usuarioDAO.getByUsername(new DataAccessUtilities.OnDataRetrievedOneListener<Usuario>() {
                @Override
                public void onDataRetrieved(Usuario userReceived) {
                    if (userReceived != null) {
                        Toast.makeText(getApplicationContext(), "El usuario no está disponible.", Toast.LENGTH_SHORT).show();
                        edtUsuario.setText("");
                        edtUsuario.requestFocus();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (errorMessage.equals("Usuario no encontrado.")) {
                        usuarioDAO.getByEmail(new DataAccessUtilities.OnDataRetrievedOneListener<Usuario>() {
                            @Override
                            public void onDataRetrieved(Usuario userReceived) {
                                if (userReceived != null) {
                                    Toast.makeText(getApplicationContext(), "El correo ya está en uso.", Toast.LENGTH_SHORT).show();
                                    edtCorreo.setText("");
                                    edtCorreo.requestFocus();
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                if (errorMessage.equals("Usuario no encontrado.")) {
                                    byte[] salt = CommonServiceUtilities.generateSalt();
                                    byte[] hashedPassword = CommonServiceUtilities.hashPassword(contrasena, salt);

                                    Usuario newUsuario = new Usuario(nombre, correo, hashedPassword, salt);
                                    usuarioDAO.setEntity(newUsuario);
                                    usuarioDAO.insertar();

                                    edtUsuario.setText("");
                                    edtContrasena.setText("");
                                    edtCorreo.setText("");
                                    edtUsuario.requestFocus();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
