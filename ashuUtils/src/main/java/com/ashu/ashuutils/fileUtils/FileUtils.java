package com.ashu.ashuutils.fileUtils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.ashu.ashuutils.R;
import com.ashu.ashuutils.models.CompressFileData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface FileUtils {

    public static final String PREF_NAME = "app_prefs";
    public static final String KEY_IMAGE_PATH = "image_path";
    public static int PROFILE_IMAGE_REQUEST = 100, STORAGE_PERMISSION_REQUEST_CODE = 102, CAMARA_PERMISSION_REQUEST_CODE = 103;

    public interface CameraSelectionCallback {
        void onCameraSelected(boolean isCamera);
    }

    public static void showPickImageDialog(Activity activity, int req_code, int pick_design, CameraSelectionCallback callback) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pick_image_dialog);
        Window window = dialog.getWindow();

        Objects.requireNonNull(window).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        LinearLayout cameraOptionBtn = dialog.findViewById(R.id.cameraOption);
        cameraOptionBtn.setOnClickListener(v -> {

            takePictureFromCamera(activity, req_code, true);
            if (callback != null) {
                callback.onCameraSelected(true);
            }

            dialog.dismiss();
        });

        LinearLayout galleryOptionBtn = dialog.findViewById(R.id.galleryOption);
        galleryOptionBtn.setOnClickListener(v -> {

            choosePictureFromGallery(activity, req_code);
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

    public static void choosePictureFromGallery(Activity context, int req_code) {
        /*Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        context.startActivityForResult(galleryIntent, req_code);*/

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        context.startActivityForResult(intent, req_code);
    }

    public static void takePictureFromCamera(Activity context, int req_code, boolean isFrontCamera) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (isFrontCamera) {
            cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri(context));
        }
        context.startActivityForResult(cameraIntent, req_code);
    }

    public static Uri setImageUri(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "image" + System.currentTimeMillis() + ".png");
        Uri photoURI = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file
        );

        setImagePath(context, file.getAbsolutePath());
        return photoURI;
    }

    public static void setImagePath(Context context, String path) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_IMAGE_PATH, path).apply();
    }

    public static String getImagePath(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_IMAGE_PATH, null);
    }



    public static boolean isStoragePermissionGranted(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1000);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                return false;
            }
        } else {
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }


    public static boolean isCameraPermissionGranted(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, 1001);
                return false;
            }
        } else {
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    // Method to explicitly request camera permission
    public static void requestCameraPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                    context,
                    new String[]{Manifest.permission.CAMERA},
                    CAMARA_PERMISSION_REQUEST_CODE
            );
        }
    }

    // Method to request storage permission
    public static void requestStoragePermission(String TAG, Activity context) {
        Log.d(TAG, "requestStoragePermission Run");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permissions are already granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(
                        context,
                        new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO
                        },
                        STORAGE_PERMISSION_REQUEST_CODE
                );
            } else {
                Log.d(TAG, "Permissions already granted for Android 14+");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if READ_EXTERNAL_STORAGE is granted for older versions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request permission
                ActivityCompat.requestPermissions(
                        context,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE
                );
            } else {
                Log.d(TAG, "Permissions already granted for Android M+");
            }
        } else {
            Log.d(TAG, "No permissions required for Android versions below M");
        }
    }

    public interface FileCallback {
        void onFileReady(File readyFile, String readyPath);
    }

    public static void handleImagePick(Intent data, boolean isCamera,
                                       String cameraPath,
                                       Activity activity,
                                       ImageView imageView, ProgressDialog progressDialog, FileCallback callback) {
        if (progressDialog != null) {
            // Show progress dialog
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
            if (isCamera) {
                path = cameraPath;
            } else {
                path = getAbsolutePath(activity, data.getData());
            }

            File imageFile = new File(Objects.requireNonNull(path));

            if (imageFile.length() > 800 * 1024) {
                imageFile = compressImage(imageFile, 800 * 1024).getFileFormat();
            }

            File finalFile = imageFile;
            String finalPath = path;

            handler.post(() -> {
                if (imageView != null) {
                    // Load image with Glide and handle progress dialog dismissal
                    Glide.with(activity)
                            .load(finalFile)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    if (finalProgressDialog != null && finalProgressDialog.isShowing()){
                                        finalProgressDialog.dismiss();
                                    }
                                    Toast.makeText(activity, "Failed to load image", Toast.LENGTH_SHORT).show();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                               Target<Drawable> target,
                                                               @NonNull DataSource dataSource, boolean isFirstResource) {
                                    if (finalProgressDialog != null && finalProgressDialog.isShowing())
                                        finalProgressDialog.dismiss();
                                    return false;
                                }
                            })
                            .into(imageView);

                }

                if (callback != null) {
                    callback.onFileReady(finalFile, finalPath);
                }
            });
        });
    }


    public static String getAbsolutePath(Activity activity, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public static CompressFileData compressImage(File originalFile, int targetSizeKB) {
        CompressFileData data = new CompressFileData();
        try {
            // Decode image with scaling options to avoid OOM for large files
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            options.inSampleSize = calculateInSampleSize(options); // example target size
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath(), options);

            // Check and handle image orientation
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
            }

            // Set initial quality and target size in bytes
            int quality = 90;
            int targetSizeBytes = targetSizeKB * 1024; // Convert KB to Bytes

            // Compress in a loop, adjusting quality to reach target size
            File compressedFile = new File(originalFile.getParent(), "compressed_" + originalFile.getName());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            do {
                byteArrayOutputStream.reset(); // Clear the stream for new compression attempt
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                quality -= 5; // Decrease quality step-by-step if target size is not met
            } while (byteArrayOutputStream.size() > targetSizeBytes && quality > 0);

            // Save the compressed image to file
            try (FileOutputStream fos = new FileOutputStream(compressedFile)) {
                fos.write(byteArrayOutputStream.toByteArray());
            }

            // Convert compressed image to Base64
            String base64String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            // Set data in CompressFileData object
            data.setBitmapFormat(bitmap);
            data.setFileFormat(compressedFile);
            data.setString64BaseFormat(base64String);

            // Recycle bitmap if no longer needed
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private static byte[] readFileAsByteArray(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return bos.toByteArray();
    }

    public static String getRealPathFromURI(Activity activity, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return uri.getPath(); // Fallback
    }

    public static Bitmap getBitmapFromUri(Activity context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);

        if (input == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        if (originalWidth <= 0 || originalHeight <= 0) {
            return null;
        }

        // Calculate a suitable inSampleSize
        int reqWidth = 480;  // Example width
        int reqHeight = 800; // Example height
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();

        return bitmap;
    }

    // Method to calculate the appropriate sample size
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Method to convert Bitmap to File
    public static File bitmapToFile(Bitmap bitmap, String fileName, Context context) {
        // Create a temporary file in the cache directory
        File file = new File(context.getCacheDir(), fileName);
        try {
            // Compress the bitmap and save it to the file
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream); // Use PNG if transparency is required
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public static void downloadImageIfNotExists(String TAG, Context context, String imageUrl, ImageView imageView, ImageView viewImageCrossIcon, String appImageFolderName) {
        ProgressDialog progressDialog = null;

        if (imageView != null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        ProgressDialog finalProgressDialog = progressDialog;

        new Thread(() -> {
            try {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                String relativePath = Environment.DIRECTORY_PICTURES + "/AshuXKit";
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appImageFolderName);
                File imageFile = new File(directory, fileName);

                // If image exists, load from file
                if (imageFile.exists()) {
                    if (imageView != null) {
                        Bitmap bitmap = decodeBitmapWithFallback(imageFile.getAbsolutePath(), imageView);
                        ((Activity) context).runOnUiThread(() -> {
                            imageView.setImageBitmap(bitmap);
                            imageView.setVisibility(View.VISIBLE);
                            viewImageCrossIcon.setVisibility(View.VISIBLE);
                            if (finalProgressDialog != null) finalProgressDialog.dismiss();
                        });
                    }
                    return;
                }

                // Download from URL
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

                Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri == null) {
                    Log.e(TAG, "Failed to create image URI in MediaStore");
                    if (finalProgressDialog != null)
                        ((Activity) context).runOnUiThread(finalProgressDialog::dismiss);
                    return;
                }

                OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    baos.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

                if (imageView != null) {
                    byte[] imageBytes = baos.toByteArray();
                    Bitmap bitmap = decodeBitmapWithFallback(imageBytes, imageView);
                    ((Activity) context).runOnUiThread(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setVisibility(View.VISIBLE);
                        viewImageCrossIcon.setVisibility(View.VISIBLE);
                        if (finalProgressDialog != null) finalProgressDialog.dismiss();
                    });
                }

                Log.d(TAG, "Image saved in "+appImageFolderName);

            } catch (Exception e) {
                Log.e(TAG, "Download failed: " + e.getMessage(), e);
                ((Activity) context).runOnUiThread(() -> {
                    if (finalProgressDialog != null) finalProgressDialog.dismiss();
                });
            }
        }).start();
    }

    static Bitmap decodeBitmapWithFallback(String path, ImageView imageView) {
        int reqWidth = imageView.getWidth() > 0 ? imageView.getWidth() : 800;
        int reqHeight = imageView.getHeight() > 0 ? imageView.getHeight() : 800;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        // Fix orientation
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            bitmap = rotateBitmap(bitmap, orientation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    // Helper for decoding from byte[]
    static Bitmap decodeBitmapWithFallback(byte[] data, ImageView imageView) {
        int reqWidth = imageView.getWidth() > 0 ? imageView.getWidth() : 800;
        int reqHeight = imageView.getHeight() > 0 ? imageView.getHeight() : 800;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static String getDocumentName(Activity activity, Uri uri) {
        String displayName = null;

        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                displayName = cursor.getString(nameIndex);
            }
            cursor.close();
        }

        if (displayName == null) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    public static boolean isPDF(String url) {
        return url != null && url.toLowerCase().endsWith(".pdf");
    }

    public static boolean renderPDFThumbnail(File file, ImageView imageView) {
        if (file == null || !file.exists()) {
            Log.e("PDF Thumbnail", "File is null or does not exist");
            return false;
        }

        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fd);
            PdfRenderer.Page page = renderer.openPage(0);

            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            imageView.setImageBitmap(bitmap);

            page.close();
            renderer.close();
            fd.close();

            return true; // ✅ Successfully rendered
        } catch (Exception e) {
            e.printStackTrace();
            return false; // ❌ Rendering failed
        }
    }

    public static String getFileNameFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return "";

        try {
            URL uri = new URL(url);
            String path = uri.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

}
