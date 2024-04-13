package com.example.reciperu.Interfaces;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reciperu.DAO.DAOImplements.UsuarioDAOImpl;
import com.example.reciperu.DAO.UsuarioDAO;
import com.example.reciperu.Entity.DataAccessUtilities;
import com.example.reciperu.Entity.Usuario;
import com.example.reciperu.R;

public class RegistroUI extends AppCompatActivity {
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

        DataAccessUtilities dau = new DataAccessUtilities();

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

            byte[] salt = dau.generateSalt();
            byte[] hashedPassword = dau.hashPassword(contrasena, salt);

            Usuario usuario = new Usuario(nombre, correo, hashedPassword, salt, "logued out");
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, this.getApplicationContext());
            boolean insertar = usuarioDAO.insertar();

            if (insertar) {
                Toast.makeText(this.getApplicationContext(), "Usuario registrado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getApplicationContext(), "Usuario no registrado", Toast.LENGTH_SHORT).show();

            }

        }
    }
}
