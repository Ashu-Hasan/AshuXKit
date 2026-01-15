package com.ashu.ashuutils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public interface TimeUtils {

    // ✅ Known formats
    static String[] knownFormats = {
            // Date + Time (24-hour & 12-hour)
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd hh:mm:ss a",
            "yyyy-MM-dd hh:mm a",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy hh:mm:ss a",
            "dd-MM-yyyy hh:mm a",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy hh:mm:ss a",
            "dd MMM yyyy HH:mm:ss",
            "dd MMM yyyy hh:mm:ss a",
            "EEE MMM dd HH:mm:ss z yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy hh:mm:ss a",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy hh:mm a",
            "dd MMM yyyy, hh:mm a",

            // Only Date formats
            "dd-MMM-yyyy",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "dd MMM yyyy",
            "dd/MM/yyyy",

            // Only Time formats
            "HH:mm:ss",
            "HH:mm",
            "hh:mm:ss a",
            "hh:mm a"
    };

    // ✅ Universal safe date parser
    static Date parseDateSafely(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;

        for (String formatStr : knownFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formatStr, Locale.getDefault());
                sdf.setLenient(false);
                Date date = sdf.parse(dateString);
                if (date != null) return date;
            } catch (ParseException ignored) {}
        }
        return null;
    }

    // ✅ getTimeAgo using common parser
    static String getTimeAgo(String dateString) {
        Date pastDate = parseDateSafely(dateString);
        if (pastDate == null) return "Invalid date";

        Date now = new Date();
        long diffMillis = now.getTime() - pastDate.getTime();

        if (diffMillis < 0) return "In the future";

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (seconds < 60) return "Just now";
        else if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        else if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        else if (days == 1) return "Yesterday";
        else if (days < 30) return days + " days ago";
        else if (days < 365) return (days / 30) + " month" + ((days / 30) == 1 ? "" : "s") + " ago";
        else return (days / 365) + " year" + ((days / 365) == 1 ? "" : "s") + " ago";
    }

    // ✅ convertDateFormat using parser
    static String convertDateFormat(String date, String outputFormat) {
        Date parsedDate = parseDateSafely(date);
        if (parsedDate == null) return date;

        SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat, Locale.getDefault());
        return outputFormatter.format(parsedDate);
    }

    // ✅ convertDateToMillies using parser
    static long convertDateToMillies(String date, String format) {
        Date parsedDate;
        if (format != null && !format.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                parsedDate = sdf.parse(date);
            } catch (ParseException e) {
                return 0;
            }
        } else {
            parsedDate = parseDateSafely(date);
        }

        return parsedDate != null ? parsedDate.getTime() : 0;
    }

    // ✅ convertTimestampIntoDate unchanged, but safe
    static String convertTimestampIntoDate(long value, String responseTitle) {
        long timestamp = (value < 10000000000L) ? value * 1000 : value;
        Date date = new Date(timestamp);

        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

        switch (responseTitle.toLowerCase()) {
            case "time": return timeFormatter.format(date);
            case "date": return dateFormatter.format(date);
            default: return dateTimeFormatter.format(date);
        }
    }

    // ✅ convert24HourTo12Hour using parser
    static String convert24HourTo12Hour(String time) {
        Date parsedDate = parseDateSafely(time);
        if (parsedDate == null) return null;

        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return outputFormat.format(parsedDate);
    }

    public static String getCurrentDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    // ✅ Check if the given date is expired compared to the provided current date
    static boolean isDateExpired(String currentDateString, String targetDateString) {
        Date currentDate = parseDateSafely(currentDateString);
        Date targetDate = parseDateSafely(targetDateString);

        // If either date is invalid → treat as expired for safety
        if (currentDate == null || targetDate == null) return true;

        return targetDate.before(currentDate); // true = expired
    }

    // ✅ Get the difference in days between two dates
    static long getDaysDifference(String startDateString, String endDateString) {
        Date startDate = parseDateSafely(startDateString);
        Date endDate = parseDateSafely(endDateString);

        if (startDate == null || endDate == null) return 0;

        // Normalize times by converting to midnight (optional)
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(diffInMillis));
    }

}

