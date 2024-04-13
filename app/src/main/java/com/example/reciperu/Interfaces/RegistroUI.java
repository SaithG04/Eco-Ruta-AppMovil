package com.example.reciperu.Interfaces;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.reciperu.R;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;



public class RegistroUI extends AppCompatActivity {
    private EditText edtUsuario, edtCorreo, edtContrasena;
    private Button registrarButton;

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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando");

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show();
        } else if (correo.isEmpty()) {
            Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show();
        } else if (contrasena.isEmpty()) {
            Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
        } else {
            try {
                progressDialog.show();

                // Cambia la URL según sea necesario:
                String url = "http://10.0.2.2/ReciPeru/insertar_.php";

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("Respuesta del servidor: " + response);
                                if (response.equalsIgnoreCase("datos insertados")) {
                                    Toast.makeText(RegistroUI.this, "Registrado correctamente", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    finish();
                                } else {
                                    Toast.makeText(RegistroUI.this, "Error: No se puede registrar", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();  // Muestra el rastreo de la pila del error para obtener más información
                                String errorMessage = error.getMessage();
                                if (errorMessage == null || errorMessage.isEmpty()) {
                                    errorMessage = "Ha ocurrido un error desconocido";
                                }
                                Toast.makeText(RegistroUI.this, errorMessage, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                System.out.println("Error: " + errorMessage);
                            }

                        }) {

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("nombre", nombre);
                        params.put("correo", correo);
                        params.put("contrasena", contrasena);
                        return params;
                    }
                };

                // Configurar el tiempo de espera de la solicitud
                request.setRetryPolicy(new DefaultRetryPolicy(
                        10000,  // Tiempo de espera en milisegundos (ajusta según sea necesario)
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                RequestQueue requestQueue = Volley.newRequestQueue(RegistroUI.this);
                requestQueue.add(request);

            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

}

