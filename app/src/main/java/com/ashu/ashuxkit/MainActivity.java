package com.ashu.ashuxkit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ashu.ashuutils.APIHelper;
import com.ashu.ashuutils.AppConstants;
import com.ashu.ashuutils.fileUtils.FileUtils;
import com.ashu.ashuutils.fileUtils.image.ImagePicker;
import com.ashu.ashuutils.fileUtils.image.ImageProcessingUtils;
import com.ashu.ashuutils.models.CompressFileData;
import com.ashu.ashuxkit.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivityData";
    ActivityMainBinding binding;
    boolean isCameraSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.pickDocument.setOnClickListener(v -> {
            AppConstants.isDebug = true;
            ImagePicker.showPickImageDialog(TAG, MainActivity.this, AppConstants.IMAGE_REQUEST, R.color.black, false, new FileUtils.ResultCallback() {
                @Override
                public void onCameraSelected(boolean isCamera) {
                    isCameraSelected = isCamera;
                }

                @Override
                public void imageUriWithoutPermission(Uri selectedImageUri) {

                    ImagePicker.showCropOptionDialog(TAG, MainActivity.this, selectedImageUri, AppConstants.IMAGE_CROP_REQUEST, "profile", R.drawable.ic_launcher_background, R.color.black, new ImagePicker.CropImageCallback() {
                        @Override
                        public void onCropOptionCanceled() {
//                            binding.selectedDocument.setText("No document selected");
                        }
                    });
                }
            });
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (isCameraSelected){
                ImageProcessingUtils.handleCameraImage("MainActivityData", MainActivity.this, FileUtils.getImagePath(MainActivity.this), binding.image, true,null, new FileUtils.FileCallback() {
                    @Override
                    public void onFileReady(CompressFileData selectedImageData) {

                        binding.selectedDocument.setText("Selected Document:\n" + FileUtils.getFileNameFromPath(MainActivity.this, selectedImageData.getFilePath()));

                    }
                });
            }
            else {
                ImageProcessingUtils.handleGalleryFromIntent("MainActivityData", MainActivity.this, data, binding.image, true, null, new FileUtils.FileCallback() {
                    @Override
                    public void onFileReady(CompressFileData selectedImageData) {

                        binding.selectedDocument.setText("Selected Document:\n" + FileUtils.getFileNameFromPath(MainActivity.this, selectedImageData.getFilePath()));

                    }
                });
            }
        } else if (requestCode == AppConstants.IMAGE_CROP_REQUEST && resultCode == RESULT_OK) {

            ImageProcessingUtils.handleCroppedImage("MainActivityData", MainActivity.this, data, binding.image, null, new FileUtils.FileCallback() {
                @Override
                public void onFileReady(CompressFileData selectedImageData) {

                    binding.selectedDocument.setText("Selected Document:\n" + FileUtils.getFileNameFromPath(MainActivity.this, selectedImageData.getFilePath()));

                }
            });
        }
    }
}