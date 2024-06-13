package com.qromarck.reciperu.Entity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class EcoNotification {

    public static String createNotificationChannel(Context context, int channel_id, String channel_name, String channel_description) {
        String id = String.valueOf(channel_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channel_description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return id;
    }

}
