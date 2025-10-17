package com.ashu.ashuutils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public interface NavigationUtils {
    public static void redirectToPlayStore(Activity activity, String appLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No app found to open the link", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openBrowser(Activity activity, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Unable to open browser", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openDialPad(Context context, String phoneNumber) {
        // Create an intent with ACTION_DIAL
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        // Set the data for the intent as a URI with the phone number
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));

        // Start the dialer app if there is an app that can handle this intent
        if (dialIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(dialIntent);
        }
    }

    public static void openWhatsApp(Context context, String phoneNumber) {
        // Add +91 if no country code is present
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+91" + phoneNumber;
        }

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);

            // Format number without '+' for wa.me URL
            String formattedNumber = phoneNumber.replace("+", "");
            String url = "https://wa.me/" + formattedNumber;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openWhatsApp(Context context, String phoneNumber, String message) {
        // Check if WhatsApp is installed
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);

            // Encode the message to handle special characters
            String encodedMessage = Uri.encode(message);

            // WhatsApp API URL with phone number and message
            String url = "https://wa.me/" + phoneNumber + "?text=" + encodedMessage;

            // Create the Intent to open WhatsApp
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            // WhatsApp is not installed on the device
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle other exceptions
            Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openWhatsAppWithMessageAndImage(Context context, String phoneNumber, String message, Uri imageUri) {
        // Check if WhatsApp is installed
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);

            // Format the phone number with WhatsApp ID
            String jid = phoneNumber + "@s.whatsapp.net"; // WhatsApp ID format

            // Create the Intent to open WhatsApp chat directly with the given number
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri); // Add the image
            sendIntent.putExtra(Intent.EXTRA_TEXT, message); // Add the message
            sendIntent.setPackage("com.whatsapp");
            sendIntent.putExtra("jid", jid); // Target specific contact

            // Start WhatsApp
            context.startActivity(sendIntent);
        } catch (PackageManager.NameNotFoundException e) {
            // WhatsApp is not installed on the device
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle other exceptions
            Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void call(Context context, String contact) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + contact));
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CALL_PHONE},
                    23);

            // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //You already have permission
            try {
                context.startActivity(callIntent);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // Reload the activity to apply the new locale
    public static void refreshActivity(Activity activity) {
        Intent intent = activity.getIntent();
        activity.overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
    }

    static void goNextActivity(Activity presentClass, Class nextClass, boolean finishCurrentPage) {
        Intent intent = new Intent(presentClass, nextClass);
        presentClass.startActivity(intent);
        if (finishCurrentPage) {
            presentClass.finish();
        }
    }

}
