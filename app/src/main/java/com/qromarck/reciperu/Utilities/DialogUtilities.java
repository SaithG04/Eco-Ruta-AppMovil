package com.qromarck.reciperu.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Menu;

import androidx.core.view.GravityCompat;

import com.qromarck.reciperu.Interfaces.CargaInicialUI;
import com.qromarck.reciperu.Interfaces.MenuUI;

public class DialogUtilities {

    public static void showNoInternetDialog(Activity activity) {
        if (!activity.isFinishing() && !activity.isDestroyed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Sin conexión a Internet");
            builder.setMessage("Por favor, conéctate a Internet para continuar.");
            builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finishAffinity();
                }
            });
            builder.setNegativeButton("Reintentar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (NetworkUtilities.isNetworkAvailable(activity.getApplicationContext())) {
                        dialog.dismiss();
                        CargaInicialUI cargaInicialUIActivity = activity instanceof CargaInicialUI ? (CargaInicialUI) activity : null;
                        if (cargaInicialUIActivity != null) {
                            cargaInicialUIActivity.cargar();
                        }
                    } else {
                        showNoInternetDialog(activity);
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    public static void showNotificationSettingsDialog(Activity activity) {
        if (!activity.isFinishing() && !activity.isDestroyed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Notificaciones deshabilitadas");
            builder.setMessage("Las notificaciones están deshabilitadas. Habilítelas para recibir notificaciones importantes.");
            builder.setPositiveButton("Ir a configuración", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NotificationUtilities.openNotificationSettings(activity);
                }
            });
            builder.setNegativeButton("En otro momento", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finishAffinity();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    public static void showInstallSettingsDialog(Activity activity) {
        if (!activity.isFinishing() && !activity.isDestroyed()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permisos");
            builder.setMessage("Necesitamos permisos para actualizar la aplicación.");
            builder.setPositiveButton("Condecer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, MenuUI.INSTALL_UNKNOWN_APPS_PERMISSION_REQUEST_CODE);
                    activity.finishAffinity();
                }
            });
            builder.setNegativeButton("En otro momento", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finishAffinity();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    public static void showEnableGPSDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("GPS Desactivado");
        builder.setMessage("Para continuar, active el GPS en su dispositivo.");
        builder.setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public static void showLogoutConfirmationDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que deseas cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MenuUI menuUI = (MenuUI) activity;
                menuUI.drawerLayout.closeDrawer(GravityCompat.START);
                menuUI.showLoadingIndicator();
                if (NetworkUtilities.isNetworkAvailable(activity.getApplicationContext())) {
                    menuUI.cerrarSesion();
                } else {
                    menuUI.hideLoadingIndicator();
                    DialogUtilities.showNoInternetDialog(activity);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
