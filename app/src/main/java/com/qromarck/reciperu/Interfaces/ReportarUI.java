package com.qromarck.reciperu.Interfaces;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.R;
import com.qromarck.reciperu.Utilities.InterfacesUtilities;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportarUI extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CODE = 100;

    private ImageView reportImageView;
    private EditText descriptionEditText;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;

    private String UPLOAD_URL = "https://reciperu2024.000webhostapp.com/uploadreporte.php"; // URL de tu script PHP en el servidor

    private String KEY_IMAGE = "foto";
    private String KEY_NAME = "nombre";
    private String KEY_DESCRIPTION = "descripcion";
    private String KEY_DATE = "fecha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportar_ui);

        reportImageView = findViewById(R.id.imgviewReporte);
        descriptionEditText = findViewById(R.id.edtDescripcionReporte);
        Button takePhotoButton = findViewById(R.id.btntomarFotoreporte);
        Button submitButton = findViewById(R.id.btnEnviarReporte);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo reporte...");

        takePhotoButton.setOnClickListener(v -> {
            if (checkPermission()) {
                takePhoto();
            } else {
                requestPermission();
            }
        });

        submitButton.setOnClickListener(v -> submitReport());
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            reportImageView.setImageBitmap(bitmap);
        }
    }

    private void submitReport() {
        String description = descriptionEditText.getText().toString();
        if (bitmap != null && !description.isEmpty()) {
            uploadImage(description);
        } else {
            Toast.makeText(this, "Por favor, toma una foto y proporciona una descripción.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(final String description) {
        progressDialog.show();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportarUI.this, response, Toast.LENGTH_SHORT).show();
                        // Redirigir a MenuUI después de enviar el reporte
                        Intent intent = new Intent(ReportarUI.this, MenuUI.class);
                        startActivity(intent);
                        finish(); // Cierra la actividad actual
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportarUI.this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Usuario nombreUsuarioLogged = InterfacesUtilities.recuperarUsuario(ReportarUI.this);
                String nameuserLogged = nombreUsuarioLogged.getFull_name();

                // Obtener la fecha actual del sistema
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                params.put(KEY_DATE, currentDate);
                params.put(KEY_NAME, nameuserLogged);
                params.put(KEY_IMAGE, imageString);
                params.put(KEY_DESCRIPTION, description);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
