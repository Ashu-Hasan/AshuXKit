package com.ashu.ashuutils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.yalantis.ucrop.BuildConfig;

public interface Messages {
    public static void showTestLog(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void showTestToast(Context context, String message) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
