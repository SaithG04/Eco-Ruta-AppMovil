package com.example.reciperu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import android.content.Intent;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reciperu.DbHelper.DbHelper;
import com.example.reciperu.Interfaces.RegistroUI;
import com.example.reciperu.Interfaces.UIMenu;

import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnRegistrarse;

    private EditText edtusuario, edtContrasena;
    private Button btnLogin;
    private DbHelper dbHelper;

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

        dbHelper = new DbHelper(this);

        //Referenciar boton Regisrarse
        btnRegistrarse = findViewById(R.id.btnRegistrar);
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


        // Inicializa las vistas
        edtusuario = findViewById(R.id.edtUsuarioLOGIN);
        edtContrasena = findViewById(R.id.edtContraLOGIN);
        btnLogin = findViewById(R.id.btnLoginLOG);

        // Establece el OnClickListener para el botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        Button borrarBDButton = findViewById(R.id.btnborrarDB);
        borrarBDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Elimina la base de datos
                boolean resultado = deleteDatabase("ReciPeru.db");

                if (resultado) {
                    Toast.makeText(getApplicationContext(), "Base de datos eliminada con éxito.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error al eliminar la base de datos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Método para verificar el inicio de sesión
    private void iniciarSesion() {
        // Obtiene los valores de los EditText
        String usuario = edtusuario.getText().toString();
        String contrasena = edtContrasena.getText().toString();

        // Verifica que los campos no estén vacíos
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena ambos campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Abre la base de datos
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta para verificar si el usuario y la contraseña son correctos
        String[] columnas = {"usuario", "contrasena"};
        String seleccion = "usuario = ? AND contrasena = ?";
        String[] seleccionArgs = {usuario, contrasena};

        Cursor cursor = db.query(
                "usuarios",  // Nombre de la tabla donde se almacenan los datos
                columnas,
                seleccion,
                seleccionArgs,
                null,
                null,
                null
        );

        // Si el cursor tiene al menos una fila, significa que los datos de inicio de sesión son correctos
        if (cursor.moveToFirst()) {
            // Cierra el cursor y la base de datos
            cursor.close();
            db.close();

            // Muestra un mensaje de éxito
            Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();

            // Inicia una nueva actividad
            Intent intent = new Intent(MainActivity.this, UIMenu.class);  // Reemplaza 'OtraActividad' con el nombre de la actividad a la que deseas redirigir después del inicio de sesión exitoso.
            intent.putExtra("usuario", usuario);
            startActivity(intent);

            // Opcionalmente, puedes finalizar la actividad actual si no quieres que el usuario pueda volver a ella
            finish();
        } else {
            // Cierra el cursor y la base de datos
            cursor.close();
            db.close();

            // Muestra un mensaje de error
            Toast.makeText(this, "Nombre de usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
            edtusuario.setText("");
            edtContrasena.setText("");
        }

    }
}