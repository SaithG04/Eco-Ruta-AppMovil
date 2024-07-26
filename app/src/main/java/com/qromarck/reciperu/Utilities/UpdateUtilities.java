package com.qromarck.reciperu.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateUtilities {

    private static final String VERSION_CHECK_URL = "https://reciperu2024.000webhostapp.com/version_check.php";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;

    public static void checkForUpdate(Context context, ProgressBar progressBar, TextView progressText) {
        new CheckVersionTask(context, progressBar, progressText).execute(VERSION_CHECK_URL);
    }

    private static class CheckVersionTask extends AsyncTask<String, Void, String> {
        private Context context;
        private ProgressBar progressBar;
        private TextView progressText;

        public CheckVersionTask(Context context, ProgressBar progressBar, TextView progressText) {
            this.context = context;
            this.progressBar = progressBar;
            this.progressText = progressText;
        }

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String serverVersion = jsonObject.getString("version_name");
                String apkUrl = jsonObject.getString("apk_url");

                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                String currentVersion = pInfo.versionName;

                if (!currentVersion.equals(serverVersion)) {
                    showUpdateDialog(apkUrl);
                } else {
                    Toast.makeText(context, "App is up to date", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to check for update", Toast.LENGTH_SHORT).show();
            }
        }

        private void showUpdateDialog(String apkUrl) {
            new AlertDialog.Builder(context)
                    .setTitle("Update Available")
                    .setMessage("A new version of the app is available. Would you like to update?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadAndInstallAPK(apkUrl);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void downloadAndInstallAPK(String apkUrl) {
            new DownloadAPKTask(context, progressBar, progressText).execute(apkUrl);
        }
    }

    private static class DownloadAPKTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private ProgressBar progressBar;
        private TextView progressText;

        public DownloadAPKTask(Context context, ProgressBar progressBar, TextView progressText) {
            this.context = context;
            this.progressBar = progressBar;
            this.progressText = progressText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
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

                input = new BufferedInputStream(connection.getInputStream());
                File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "app_update.apk");
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
                e.printStackTrace();
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

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);
            progressText.setText("Downloaded " + progress[0] + "%");
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
            if (result != null) {
                File apkFile = new File(result);
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Download error", Toast.LENGTH_LONG).show();
            }
        }
    }
}
