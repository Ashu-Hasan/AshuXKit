package com.ashu.ashuutils.fileUtils.image;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.ashu.ashuutils.Messages;
import com.ashu.ashuutils.R;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.fileUtils.PermissionUtils;

import java.io.File;
import java.util.Objects;

public class ImagePickerWithPermission {

    public static void showPickImageDialog(String TAG, Activity activity, int req_code, int designColor, FileUtils.CameraSelectionCallback callback) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pick_image_dialog);
        Window window = dialog.getWindow();

        Objects.requireNonNull(window).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        // 🔥 Resolve color from resources
        int color = ContextCompat.getColor(activity, designColor);

        AppCompatImageView imageView = dialog.findViewById(R.id.image_view_dialog);
        imageView.setColorFilter(color);

        ImageView cameraIcon = dialog.findViewById(R.id.cameraIcon);
        cameraIcon.setColorFilter(color);

        ImageView galleryIcon = dialog.findViewById(R.id.galleryIcon);
        galleryIcon.setColorFilter(color);

        ImageView closeIcon = dialog.findViewById(R.id.closeIcon);
        closeIcon.setColorFilter(color);

        LinearLayout cameraOptionBtn = dialog.findViewById(R.id.cameraOption);
        cameraOptionBtn.setOnClickListener(v -> {

            takePictureFromCamera(TAG, activity, req_code, true);
            if (callback != null) {
                callback.onCameraSelected(true);
            }

            dialog.dismiss();
        });

        LinearLayout galleryOptionBtn = dialog.findViewById(R.id.galleryOption);
        galleryOptionBtn.setOnClickListener(v -> {

            choosePictureFromGallery(TAG, activity, req_code);
            if (callback != null) {
                callback.onCameraSelected(false);
            }
            dialog.dismiss();
        });

        LinearLayout closeBtn = dialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void choosePictureFromGallery(String TAG, Activity context, int req_code) {
        if (PermissionUtils.isStoragePermissionGranted(TAG, context)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            context.startActivityForResult(intent, req_code);
        } else {
            PermissionUtils.requestStoragePermission(TAG, context);
        }
    }

    public static void takePictureFromCamera(String TAG, Activity context, int req_code, boolean isFrontCamera) {

        if (PermissionUtils.isCameraPermissionGranted(TAG, context)) {
            try {
                // Create file
                File photoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "image_" + System.currentTimeMillis() + ".jpg");

                Uri imageUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        photoFile
                );

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Log camera direction
                Messages.showTestLog(TAG, "📸 Camera direction: " + (isFrontCamera ? "Front" : "Back"));

                if (isFrontCamera) {
                    // 🟢 Force front camera — works on most OEMs
                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                } else {
                    // 🔵 Ensure back camera
                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_BACK", 0);
                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);
                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false);
                }

                // Save image path
                FileUtils.setImagePath(context, photoFile.getAbsolutePath());
                Messages.showTestLog(TAG, "📂 Image path saved: " + photoFile.getAbsolutePath());

                // Start activity
                context.startActivityForResult(cameraIntent, req_code);
                Messages.showTestLog(TAG, "🚀 Camera intent started successfully.");

            } catch (Exception e) {
                Messages.showTestLog(TAG, "🔥 Error while opening camera: " + e.getMessage());
            }

        } else PermissionUtils.requestCameraPermission(context);
    }
}
