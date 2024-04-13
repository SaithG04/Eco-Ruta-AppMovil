package com.example.reciperu.Interfaces;

import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.reciperu.DbHelper.DbHelper;
import com.example.reciperu.R;
import androidx.appcompat.app.AppCompatActivity;

public class RegistroUI extends AppCompatActivity {
    private EditText edtUsuario, edtCorreo, edtContrasena;
    private Button registrarButton;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_ui);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa las vistas
        edtUsuario = findViewById(R.id.edtUsuarioREG);
        edtCorreo = findViewById(R.id.edtCorreoREG);
        edtContrasena = findViewById(R.id.edtContrasenaREG);
        registrarButton = findViewById(R.id.btnREG);

        // Crea una instancia de la base de datos
        dbHelper = new DbHelper(this);

        // Establece el OnClickListener para el botón de registro
        registrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

    }

    private void registrarUsuario() {
        // Obtiene los valores de los EditText
        String usuario = edtUsuario.getText().toString();
        String correo = edtCorreo.getText().toString();
        String contrasena = edtContrasena.getText().toString();

        // Verifica que los campos no estén vacíos
        if (usuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtiene una instancia de la base de datos en modo lectura para verificar si el usuario existe
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta para verificar si el nombre de usuario ya existe
        String[] columnas = {"usuario"};
        String seleccion = "usuario = ?";
        String[] seleccionArgs = {usuario};

        Cursor cursor = db.query(
                "usuarios",  // Nombre de la tabla donde se almacenan los datos
                columnas,
                seleccion,
                seleccionArgs,
                null,
                null,
                null
        );

        // Verifica si el usuario ya existe
        if (cursor.moveToFirst()) {
            // Cierra el cursor y la base de datos
            cursor.close();
            db.close();

            // Muestra un mensaje de advertencia
            Toast.makeText(this, "El nombre de usuario ya existe. Por favor, elige otro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cierra el cursor
        cursor.close();

        // Obtiene una instancia de la base de datos en modo escritura para insertar el nuevo usuario
        db = dbHelper.getWritableDatabase();

        // Inserta los datos en la tabla
        ContentValues values = new ContentValues();
        values.put("usuario", usuario);
        values.put("correo", correo);
        values.put("contrasena", contrasena);

        long newRowId = db.insert("usuarios", null, values);

        // Cierra la base de datos
        db.close();

        // Verifica si la inserción fue exitosa
        if (newRowId != -1) {
            Toast.makeText(this, "Usuario registrado con éxito.", Toast.LENGTH_SHORT).show();
            // Limpia los campos de texto
            edtUsuario.setText("");
            edtCorreo.setText("");
            edtContrasena.setText("");
        } else {
            Toast.makeText(this, "Error al registrar el usuario.", Toast.LENGTH_SHORT).show();
            // Limpia los campos de texto
            edtUsuario.setText("");
            edtCorreo.setText("");
            edtContrasena.setText("");
        }

    }
}