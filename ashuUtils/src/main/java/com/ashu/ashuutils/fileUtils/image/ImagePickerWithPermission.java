package com.ashu.ashuutils.fileUtils.image;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

    /**
     * Displays a custom dialog to pick an image either from Camera or Gallery.
     *
     * @param TAG             Used for logging/debugging. Helps developers track logs easily
     *                        when using this utility inside different modules or apps.
     *
     * @param activity        Activity context required to create and display the dialog,
     *                        access resources, and launch intents.
     *
     * @param req_code        Request code used to identify the result in onActivityResult()
     *                        (for camera or gallery response handling).
     *
     * @param designColor     Resource color ID used to dynamically tint dialog icons
     *                        (for UI customization based on app theme).
     *
     * @param withPermission  Defines how image selection should work:
     *                        - true  → Use runtime permissions (for apps heavily using media like gallery apps)
     *                        - false → Skip permission flow (for limited use like profile image selection)
     *
     * @param callback        Interface callback to return user actions (camera/gallery selection)
     *                        and image result back to the calling Activity.
     */
    public static void showPickImageDialog(String TAG, Activity activity, int req_code, int designColor, boolean withPermission, FileUtils.ResultCallback callback) {

        // Create dialog instance using activity context
        final Dialog dialog = new Dialog(activity);

        // Remove default dialog title
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set custom layout for dialog UI
        dialog.setContentView(R.layout.pick_image_dialog);

        // Get dialog window reference
        Window window = dialog.getWindow();

        // Ensure window is not null and set layout width/height
        Objects.requireNonNull(window).setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        // Set transparent background for rounded/custom UI
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Prevent dialog dismissal on outside touch or back press
        dialog.setCancelable(false);

        // 🔥 Resolve color from resources (theme-based dynamic color)
        int color = ContextCompat.getColor(activity, designColor);

        // Get main image view and apply tint color
        AppCompatImageView imageView = dialog.findViewById(R.id.image_view_dialog);
        imageView.setColorFilter(color);

        // Get camera icon and apply tint
        ImageView cameraIcon = dialog.findViewById(R.id.cameraIcon);
        cameraIcon.setColorFilter(color);

        // Get gallery icon and apply tint
        ImageView galleryIcon = dialog.findViewById(R.id.galleryIcon);
        galleryIcon.setColorFilter(color);

        // Get close icon and apply tint
        ImageView closeIcon = dialog.findViewById(R.id.closeIcon);
        closeIcon.setColorFilter(color);

        // Camera option button
        LinearLayout cameraOptionBtn = dialog.findViewById(R.id.cameraOption);
        cameraOptionBtn.setOnClickListener(v -> {

            // Launch camera intent
            takePictureFromCamera(TAG, activity, req_code, true);

            // Notify callback that camera is selected
            if (callback != null) {
                callback.onCameraSelected(true);
            }

            // Close dialog
            dialog.dismiss();
        });

        // Gallery option button
        LinearLayout galleryOptionBtn = dialog.findViewById(R.id.galleryOption);
        galleryOptionBtn.setOnClickListener(v -> {

            if (withPermission) {
                // Open gallery with runtime permission handling
                choosePictureFromGalleryWithPermission(TAG, activity, req_code);

                // Notify callback that gallery is selected
                if (callback != null) {
                    callback.onCameraSelected(false);
                }
            } else {
                // Open gallery without permission (for limited/simple use cases)
                ImagePickerWithoutPermission.pickImage(
                        activity,
                        callback::imageUriWithoutPermission
                );
            }

            // Close dialog
            dialog.dismiss();
        });

        // Close button action
        LinearLayout closeBtn = dialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(v -> {
            // Simply dismiss dialog
            dialog.dismiss();
        });

        // Show dialog on screen
        dialog.show();
    }

    public static void choosePictureFromGalleryWithPermission(String TAG, Activity context, int req_code) {
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
