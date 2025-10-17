package com.ashu.ashuutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public interface StringUtil {

    @SuppressLint("ObsoleteSdkInt")
    static String convertHtmlToString(String htmlText) {
        // Check Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use the newer fromHtml method for Android Nougat and above
            Spanned spannedText = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
            return spannedText.toString();
        } else {
            // Use the deprecated fromHtml method for older versions
            Spanned spannedText = Html.fromHtml(htmlText);
            return spannedText.toString();
        }
    }


    /**
     * @param context        Context from your activity or application
     * @param addressString  Full address string
     * @param type           What to return: "city", "state", or "address"
     */
    public static String getCityStateFromAddress(Context context, String addressString, String type) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String city = address.getLocality();         // e.g., Manglour
                String state = address.getAdminArea();       // e.g., Uttarakhand

                if (type.equalsIgnoreCase("city") && city != null) {
                    return city;
                } else if (type.equalsIgnoreCase("state") && state != null) {
                    return state;
                } else if (type.equalsIgnoreCase("address")) {
                    if (city != null && state != null) {
                        return city + ", " + state;
                    } else if (state != null) {
                        return state;
                    } else if (city != null) {
                        return city;
                    }
                }

                // If type doesn't match or no data found, return full address
                return addressString;
            } else {
                return addressString;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return addressString;
        }
    }

    public static String getAddressFromLatLng(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String city = address.getLocality(); // City
                String state = address.getAdminArea(); // State
                String fullAddress = address.getAddressLine(0); // Full Address

                if (city != null && !city.isEmpty()) {
                    return city;
                } else if (state != null && !state.isEmpty()) {
                    return state;
                } else {
                    return fullAddress != null ? fullAddress : "Address not available";
                }
            } else {
                return "Address not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Geocoder service not available";
        }
    }

    public static String calculatePercentage(String value, String percent) {
        try {
            // Convert inputs to double
            double baseValue = Double.parseDouble(value);
            double percentage = Double.parseDouble(percent);

            // Calculate the percentage
            double result = (baseValue * percentage) / 100.0;

            // Return the result as a String
            return String.valueOf(result);
        } catch (NumberFormatException e) {
            return "Invalid input. Please provide numeric values.";
        }
    }

}
