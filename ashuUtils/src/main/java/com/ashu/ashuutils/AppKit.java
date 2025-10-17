package com.ashu.ashuutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;

import java.util.List;
import java.util.Locale;

public interface AppKit {

    // Method to get the current version of the app
    public static String getAppVersion(Context context, String versionResultValue) {
        // 0 for version Name
        // 1 for version Code
        // 2 for version Name and Code
        try {
            // Retrieve the package manager and package info
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            // Extract version name and version code
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            // Return as a formatted string
            if (versionResultValue.equalsIgnoreCase("0")) {
                return String.valueOf(versionCode);
            } else if (versionResultValue.equalsIgnoreCase("1")) {
                return versionName;
            } else {
                return "Version: " + versionName + " (Code: " + versionCode + ")";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Version info not available";
        }
    }

    // Method to get device name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    // Utility method to capitalize first letter
    private static String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) {
            return str;
        } else {
            return Character.toUpperCase(first) + str.substring(1);
        }
    }

    public static String getDeviceData(Context context) {
        String deviceId = getDeviceId(context); // Unique device identifier
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL; // Brand + Model
        String deviceModel = Build.MODEL; // Model
        String deviceModelNumber = Build.PRODUCT; // Model number (often similar to model)

        return "Device ID: " + deviceId + "\n" +
                "Device Name: " + deviceName + "\n" +
                "Device Model: " + deviceModel + "\n" +
                "Device Model Number: " + deviceModelNumber;
    }

    @SuppressLint("HardwareIds")
    private static String getDeviceId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "Unavailable";
        }
    }

    public static void setLocaleLanguage(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }


    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
