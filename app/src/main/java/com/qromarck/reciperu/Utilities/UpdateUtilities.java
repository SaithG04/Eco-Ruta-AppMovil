package com.qromarck.reciperu.Utilities;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;

public class UpdateUtilities {

    private static CheckVersionTask checkVersionTask;
    private static final String URL_CHECK_UPDATES = "https://reciperu2024.000webhostapp.com/version_check.php";

    public static void checkForUpdate(Activity activity, ProgressBar progressBar, TextView progressText, View overlay) {
        checkVersionTask = new CheckVersionTask(activity, progressBar, progressText, overlay);
        checkVersionTask.execute(URL_CHECK_UPDATES);
    }

    public static void cancelUpdateTasks() {
        if (checkVersionTask != null) {
            checkVersionTask.cancelTasks();
        }
    }
}
