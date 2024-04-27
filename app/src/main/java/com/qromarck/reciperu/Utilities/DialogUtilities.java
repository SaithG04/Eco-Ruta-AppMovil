package com.qromarck.reciperu.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.qromarck.reciperu.Interfaces.CargaInicialUI;

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
                        // Si hay conexión a internet, cierra el diálogo y continúa con la operación
                        dialog.dismiss();
                        CargaInicialUI cargaInicialUIActivity = activity instanceof CargaInicialUI ? (CargaInicialUI) activity : null;
                        if(cargaInicialUIActivity != null) {
                            cargaInicialUIActivity.cargar();
                        }
                    } else {
                        // Si no hay conexión a internet, muestra el diálogo nuevamente
                        showNoInternetDialog(activity);
                    }
                }
            });
            builder.setCancelable(false); // Evita que el usuario cierre el diálogo con el botón de retroceso
            builder.show();
        }
    }

}
