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

            byte[] salt = csu.generateSalt();
            byte[] hashedPassword = csu.hashPassword(contrasena, salt);

            Usuario usuario = new Usuario(nombre, correo, hashedPassword, salt, "logued out");
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl(usuario, this.getApplicationContext());
            usuarioDAO.insertar();

//            Toast.makeText(this.getApplicationContext(), insertar ? "Usuario Registrado" : "Usuario no registrado", Toast.LENGTH_SHORT).show();


            edtUsuario.setText("");
            edtContrasena.setText("");
            edtCorreo.setText("");

        }



    }
}
