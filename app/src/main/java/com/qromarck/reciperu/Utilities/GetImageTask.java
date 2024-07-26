package com.qromarck.reciperu.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qromarck.reciperu.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetImageTask extends AsyncTask<String, Void, Bitmap> {

    private final ImageView imgUser;
    private final ImageView imgloading;
    private final Context context;

    public GetImageTask(Context context, ImageView imgUser, ImageView imgloading) {
        this.context = context;
        this.imgUser = imgUser;
        this.imgloading = imgloading;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Mostrar el GIF de carga en el ImageView
        Glide.with(imgloading.getContext()).asGif().load(R.drawable.loading_profile).into(imgloading);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String nombre = params[0];
        String serverUrl = "https://reciperu2024.000webhostapp.com/obtener_imagen.php";

        try {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String postData = "nombre=" + nombre;
            connection.getOutputStream().write(postData.getBytes());

            InputStream inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            connection.disconnect();

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            imgUser.setImageBitmap(result);
            imgloading.setImageBitmap(null);
        } else {
            imgUser.setImageResource(R.drawable.vectorlogin);
            imgloading.setImageBitmap(null);
        }
    }
}
