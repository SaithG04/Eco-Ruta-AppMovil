package com.qromarck.reciperu.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAPKTask extends AsyncTask<String, Integer, String> {

    private Activity activity;
    private ProgressBar progressBar;
    private TextView progressText;
    private View overlay;

    public DownloadAPKTask(Activity activity, ProgressBar progressBar, TextView progressText, View overlay) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.progressText = progressText;
        this.overlay = overlay;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            File apkFile = new File(activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "EcoRuta.apk");

            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(apkFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
            output.flush();
            return apkFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
            if (connection != null) connection.disconnect();
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressBar.setProgress(progress[0]);
        progressText.setText(String.format("%d%%", progress[0]));
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onPostExecute(String result) {
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
        if (result != null) {
            try {
                Uri apkUri;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    apkUri = Uri.parse("file://" + result);
                } else {
                    File apkFile = new File(result);
                    apkUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", apkFile);
                }

                Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                promptInstall.setDataAndType(apkUri, "application/vnd.android.package-archive");
                promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(promptInstall);
            } catch (Exception exception) {
                Log.e("APK Installation", "Error during APK installation", exception);
                Toast.makeText(activity, "Error durante la instalación: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                exception.printStackTrace(System.err);
            }
        } else {
            Toast.makeText(activity, "Error de descarga, intente nuevamente.", Toast.LENGTH_LONG).show();
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.finishAffinity();
                }
            }, 2000);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
    }
}
