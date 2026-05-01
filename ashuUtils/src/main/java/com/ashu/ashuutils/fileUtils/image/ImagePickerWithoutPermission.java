package com.ashu.ashuutils.fileUtils.image;

import android.app.Activity;
import android.net.Uri;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import org.jetbrains.annotations.Nullable;

public class ImagePickerWithoutPermission {
    public interface ImagePickCallback {
        void onImagePicked(@Nullable Uri uri);
    }

    private static ActivityResultLauncher<PickVisualMediaRequest> launcher;

    public static void init(Activity activity) {

        launcher = ((ComponentActivity) activity).registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (callback != null) {
                        callback.onImagePicked(uri);
                    }
                });
    }

    private static ImagePickCallback callback;

    public static void pickImage(Activity activity, ImagePickCallback cb) {

        callback = cb;

        if (launcher == null) {
            init(activity);
        }

        launcher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

}
