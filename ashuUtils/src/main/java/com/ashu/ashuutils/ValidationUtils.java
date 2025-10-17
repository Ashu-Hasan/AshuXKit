package com.ashu.ashuutils;

public interface ValidationUtils {

    public static boolean checkStringValue(String value){
        return value != null && !value.trim().isEmpty() &&
                !value.equalsIgnoreCase("") && !value.equalsIgnoreCase("null");
    }


    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPAN(String pan) {
        return pan != null && pan.matches("[A-Z]{5}[0-9]{4}[A-Z]");
    }
}
