package com.ashu.ashuutils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public interface ShareDataUtils {

    public static void shareImagesOnly(Context context, ArrayList<String> imageUrls, String appImageFolderName) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Preparing images to share...");
        progressDialog.setCancelable(false);
        ((Activity) context).runOnUiThread(progressDialog::show);

        new Thread(() -> {
            try {
                ArrayList<Uri> imageUris = new ArrayList<>();
                File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appImageFolderName);
                if (!imageDir.exists()) imageDir.mkdirs();

                for (String Url : imageUrls) {
                    String imageUrl = Url;
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    File imageFile = new File(imageDir, fileName);

                    if (!imageFile.exists()) {
                        Log.d("ShareImages", "Downloading: " + fileName);
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        InputStream inputStream = connection.getInputStream();
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        byte[] buffer = new byte[4096];
                        int len;

                        while ((len = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                        inputStream.close();
                    } else {
                        Log.d("ShareImages", "Already exists: " + fileName);
                    }

                    Uri uri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".fileprovider",
                            imageFile
                    );
                    imageUris.add(uri);
                }

                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        Intent imageIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        imageIntent.setType("image/*");
                        imageIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        imageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        /*
                         for only whats app
                        imageIntent.setPackage("com.whatsapp");
                        context.startActivity(imageIntent);
                        */

                        // for all apps
                        Intent chooser = Intent.createChooser(imageIntent, "Share images via");
                        context.startActivity(chooser);
                    } catch (Exception e) {
                        Log.e("ShareImages", "Error sharing images", e);
                        Toast.makeText(context, "Image share failed 😢", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("ShareImages", "Error downloading or preparing images", e);
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Failed to prepare images 🥲", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // 🔹 3. Shares text message only
    public static void shareTextOnly(Context context, String shareMessage) {
        try {
            Intent textIntent = new Intent(Intent.ACTION_SEND);
            textIntent.setType("text/plain");
            textIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            /*
            for only whats app
            textIntent.setPackage("com.whatsapp");
            context.startActivity(textIntent);
            */

            // for all apps
            context.startActivity(Intent.createChooser(textIntent, "Share message via"));
        } catch (Exception e) {
            Log.e("ShareJob", "Error sharing text", e);
            Toast.makeText(context, "Message share failed", Toast.LENGTH_SHORT).show();
        }
    }

}
