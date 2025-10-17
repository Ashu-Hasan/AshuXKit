package com.ashu.ashuutils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public interface Messages {
    public static void showTestLog(String TAG, String message, boolean ENABLE_TESTING) {
        if (ENABLE_TESTING) {
            Log.d(TAG, message);
        }
    }

    public static void showTestToast(Context context, String message, boolean ENABLE_TESTING) {
        if (ENABLE_TESTING) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
