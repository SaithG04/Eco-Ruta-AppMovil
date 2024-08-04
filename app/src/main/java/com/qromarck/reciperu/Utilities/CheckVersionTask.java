package com.qromarck.reciperu.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qromarck.reciperu.Entity.Usuario;
import com.qromarck.reciperu.Interfaces.CargaInicialUI;
import com.qromarck.reciperu.Interfaces.LoginUI;
import com.qromarck.reciperu.Interfaces.MenuUI;
import com.qromarck.reciperu.Interfaces.TransitionUI;
import com.qromarck.reciperu.Utilities.DialogUtilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckVersionTask extends AsyncTask<String, Void, String> {

    private Activity activity;
    private ProgressBar progressBar;
    private TextView progressText;
    private View overlay;
    private DownloadAPKTask downloadAPKTask;

    public CheckVersionTask(Activity activity, ProgressBar progressBar, TextView progressText, View overlay) {
        this.activity = activity;
        this.progressBar = progressBar;
        this.progressText = progressText;
        this.overlay = overlay;
    }

    @Override
    protected String doInBackground(String... urls) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } else {
                return "Server returned HTTP " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (result.startsWith("Server returned HTTP")) {
                throw new Exception(result);
            }

            JSONObject jsonObject = new JSONObject(result);
            String serverVersion = jsonObject.getString("version_name");
            String apkUrl = jsonObject.getString("apk_url");

            PackageManager pm = activity.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(activity.getPackageName(), 0);
            String currentVersion = pInfo.versionName;

            if (!currentVersion.equals(serverVersion)) {
                showUpdateDialog(apkUrl);
            } else {
                proceedToNextActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleError(e);
        }
    }

    private void showUpdateDialog(String apkUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("Nueva versión disponible")
                .setMessage("Hay una nueva versión disponible. ¿Desea descargarla?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!activity.getPackageManager().canRequestPackageInstalls()) {
                                DialogUtilities.showInstallSettingsDialog(activity);
                            } else {
                                downloadAPK(apkUrl);
                            }
                        } else {
                            downloadAPK(apkUrl);
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("En otro momento", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.finishAffinity();
                    }
                })
                .show();
    }

    private void downloadAPK(String apkUrl) {
        downloadAPKTask = new DownloadAPKTask(activity, progressBar, progressText, overlay);
        downloadAPKTask.execute(apkUrl);
    }

    public void cancelTasks() {
        if (downloadAPKTask != null) {
            downloadAPKTask.cancel(true);
        }
    }

    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            if (!NotificationUtilities.areNotificationsEnabled(activity.getApplicationContext())) {
                DialogUtilities.showNotificationSettingsDialog(activity);
            } else {
                Usuario usuario = InterfacesUtilities.recuperarUsuario(activity.getApplicationContext());
                if (usuario != null) {
                    activity.startActivity(new Intent(activity, MenuUI.class));
                } else {
                    activity.startActivity(new Intent(activity, LoginUI.class));
                }
                activity.finish();
            }
        }, TransitionUI.SPLASH_SCREEN_TIMEOUT);
    }

    private void handleError(Exception e) {
        String error = e.getMessage();
        Toast.makeText(activity, error.contains("End of input at character 0 of") ? "Ha ocurrido un error de conexión." : "Ha ocurrido un error inesperado.", Toast.LENGTH_SHORT).show();
        activity.finishAffinity();
    }
}
