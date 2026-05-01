package com.ashu.ashuutils.fileUtils.image;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ashu.ashuutils.Messages;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.models.CompressFileData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageProcessingUtils {

    public static void handleImagePick(String TAG, Activity activity, String appImageFolderName, Intent data, boolean isCamera, boolean isCropped,
                                       String cameraPath,
                                       ImageView imageView,
                                       ProgressDialog progressDialog,
                                       FileUtils.FileCallback callback) {

        Messages.showTestLog(TAG, "🟢 handleImagePick called - isCamera: " + isCamera + ", cameraPath: " + cameraPath);

        if (progressDialog != null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Loading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        ProgressDialog finalProgressDialog = progressDialog;

        executor.execute(() -> {
            String path = "";

            try {
                if (isCropped) {
                    Uri croppedImageUri = UCrop.getOutput(data);  // Get Cropped Image URI
                    if (croppedImageUri != null) {

// 🟢 Case 1: Single image (some devices)
                        if (data.getData() != null) {
                            croppedImageUri = data.getData();
                            Messages.showTestLog(TAG, "🖼️ Picked image URI (getData): " + croppedImageUri);
                        }

// 🟢 Case 2: Single / Multiple images (Xiaomi / Android 13)
                        else if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                            croppedImageUri = data.getClipData().getItemAt(0).getUri();
                            Messages.showTestLog(TAG, "🖼️ Picked image URI (ClipData): " + croppedImageUri);
                        }

                        if (croppedImageUri == null) {
                            throw new IllegalStateException("Gallery returned null URI");
                        }

// ✅ COPY URI → FILE (do NOT resolve real path)
                        File imageFile = FileUtils.copyUriToCacheFile(activity, croppedImageUri);
                        if (imageFile == null || !imageFile.exists()) {
                            throw new IOException("Failed to copy image from URI");
                        }

                        path = imageFile.getAbsolutePath();
                    }
                } else {
                    if (isCamera) {
                        path = cameraPath;
                        Messages.showTestLog(TAG, "📸 Using camera image path: " + path);
                    } else {
                        Uri selectedUri = null;

// 🟢 Case 1: Single image (some devices)
                        if (data.getData() != null) {
                            selectedUri = data.getData();
                            Messages.showTestLog(TAG, "🖼️ Picked image URI (getData): " + selectedUri);
                        }

// 🟢 Case 2: Single / Multiple images (Xiaomi / Android 13)
                        else if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                            selectedUri = data.getClipData().getItemAt(0).getUri();
                            Messages.showTestLog(TAG, "🖼️ Picked image URI (ClipData): " + selectedUri);
                        }

                        if (selectedUri == null) {
                            throw new IllegalStateException("Gallery returned null URI");
                        }

// ✅ COPY URI → FILE (do NOT resolve real path)
                        File imageFile = FileUtils.copyUriToCacheFile(activity, selectedUri);
                        if (imageFile == null || !imageFile.exists()) {
                            throw new IOException("Failed to copy image from URI");
                        }

                        path = imageFile.getAbsolutePath();
                        Messages.showTestLog(TAG, "📂 Copied image path: " + path);

                    }
                }

                if (path == null || path.isEmpty()) {
                    Messages.showTestLog(TAG, "❌ Path is null or empty!");
                    handler.post(() -> {
                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                            finalProgressDialog.dismiss();
                        Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                File imageFile = new File(path);
                if (!imageFile.exists()) {
                    Messages.showTestLog(TAG, "❌ File does not exist at: " + path);
                } else {
                    Messages.showTestLog(TAG, "✅ File exists at: " + path);
                }

                CompressFileData selectedImageData = compressImage(TAG, activity, appImageFolderName, imageFile, 1024);
                selectedImageData.setFilePath(path);

                Messages.showTestLog(TAG, "🧩 Compression complete: " +
                        (selectedImageData.getFileFormat() != null ?
                                selectedImageData.getFileFormat().getAbsolutePath() : "null"));

                handler.post(() -> {
                    if (imageView != null) {
                        // Set image on UI
                        Glide.with(activity)
                                .load(imageFile)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                @NonNull Target<Drawable> target, boolean isFirstResource) {
                                        Messages.showTestLog(TAG, "❌ Glide failed to load image: " + e);
                                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                                            finalProgressDialog.dismiss();
                                        Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                                   Target<Drawable> target,
                                                                   @NonNull DataSource dataSource, boolean isFirstResource) {
                                        Messages.showTestLog(TAG, "✅ Image successfully loaded into ImageView.");
                                        if (finalProgressDialog != null && finalProgressDialog.isShowing())
                                            finalProgressDialog.dismiss();
                                        return false;
                                    }
                                })
                                .into(imageView);
                    }

                    // Callback
                    if (callback != null) {
                        Messages.showTestLog(TAG, "📤 Returning file to callback: " + selectedImageData.getFilePath());
                        callback.onFileReady(selectedImageData);
                    }
                });

            } catch (Exception e) {
                Messages.showTestLog(TAG, "🔥 Exception in handleImagePick: " + e.getMessage());
                handler.post(() -> {
                    if (finalProgressDialog != null && finalProgressDialog.isShowing())
                        finalProgressDialog.dismiss();
                    Toast.makeText(activity, "Unable to load this image, Please another image", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public static CompressFileData compressImage(String TAG, Activity activity, String appImageFolderName, File originalFile, int targetSizeKB) {
        CompressFileData data = new CompressFileData();

        try {
            Log.i(TAG, "🧮 Starting compression for: " + originalFile.getAbsolutePath());
            if (!originalFile.exists()) {
                Log.e(TAG, "❌ Original file does not exist!");
                return data;
            }

            // Decode image with scaling options to avoid OOM for large files
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);
            Log.i(TAG, "📏 Original Image Dimensions: " + options.outWidth + "x" + options.outHeight);

            options.inSampleSize = calculateInSampleSize(options);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            if (bitmap == null) {
                Log.e(TAG, "❌ Failed to decode bitmap from file!");
                return data;
            }

            // Handle rotation
            ExifInterface exif = new ExifInterface(originalFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationAngle = switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };

            if (rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Log.i(TAG, "🔄 Rotated image by " + rotationAngle + " degrees");
            }

            // Prepare for compression
            int quality = 90;
            int targetSizeBytes = targetSizeKB * 1024;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            File appDir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), appImageFolderName);
            if (!appDir.exists()) appDir.mkdirs();

            File compressedFile = new File(appDir,
                    "compressed_" + System.currentTimeMillis() + "_" + originalFile.getName());


            // Compress loop
            do {
                byteArrayOutputStream.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                Log.i(TAG, "📉 Compressing... quality=" + quality + ", size=" + byteArrayOutputStream.size() / 1024 + " KB");
                quality -= 5;
            } while (byteArrayOutputStream.size() > targetSizeBytes && quality > 10);

            // Write file
            try (FileOutputStream fos = new FileOutputStream(compressedFile)) {
                fos.write(byteArrayOutputStream.toByteArray());
            }

            Log.i(TAG, "✅ Compression finished, saved at: " + compressedFile.getAbsolutePath());

            // Save data
            String base64String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            data.setBitmapFormat(bitmap);
            data.setFileFormat(compressedFile);
            data.setString64BaseFormat(base64String);

            bitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "🔥 Compression failed: " + e.getMessage(), e);
            data.setBitmapFormat(null);
            data.setFileFormat(null);
            data.setString64BaseFormat(null);
        }

        return data;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > 1000 || width > 1000) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= 1000 && (halfWidth / inSampleSize) >= 1000) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
