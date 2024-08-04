package com.qromarck.reciperu.Utilities;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.content.Context;

public class DeviceUtils {
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
