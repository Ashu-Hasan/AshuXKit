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

    public static String getTimeAgo(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Invalid date";
        }

        String[] possibleFormats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "MM-dd-yyyy HH:mm",
                "MMM dd, yyyy HH:mm:ss",
                "EEE MMM dd HH:mm:ss zzz yyyy"
        };

        Date pastDate = null;
        for (String formatStr : possibleFormats) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
                pastDate = format.parse(dateString);
                if (pastDate != null) break;
            } catch (ParseException ignored) {
            }
        }

        if (pastDate == null) {
            return "Invalid date";
        }

        Date now = new Date();
        long diffMillis = now.getTime() - pastDate.getTime();

        if (diffMillis < 0) {
            return "In the future";
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 30) {
            return days + " days ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        } else {
            long years = days / 365;
            return years + " year" + (years == 1 ? "" : "s") + " ago";
        }
    }


    public static String convertDateFormat(String date, String outputFormat) {
        String[] knownFormats = {
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

                // Only Date formats
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

        for (String format : knownFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.getDefault());
                inputFormat.setLenient(false);
                Date parsedDate = inputFormat.parse(date);
                SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat, Locale.getDefault());
                return outputFormatter.format(parsedDate);
            } catch (ParseException ignored) {}
        }

        // If no format matched, return original date or throw exception
        return date;
    }

    public static long convertDateToMillies(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the exception as needed
            return 0;
        }
    }

    static String convertTimestampIntoDate(long value, String responseTitle) {
        String result = "";

        // Convert the timestamp to milliseconds if it is in seconds
        long timestamp = value * 1000;

        // Create a Date object
        Date date = new Date(timestamp);

        // Formatters for time, date, and combined date-time
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        // Adjust time zone to the default time zone
        timeFormatter.setTimeZone(TimeZone.getDefault());
        dateFormatter.setTimeZone(TimeZone.getDefault());
        dateTimeFormatter.setTimeZone(TimeZone.getDefault());

        // Get the formatted time, date, and combined date-time
        String time = timeFormatter.format(date);
        String numberDate = dateFormatter.format(date);
        String dateTime = dateTimeFormatter.format(date);

        // Return the desired format based on the response title
        if (responseTitle.equals("time")) {
            result = time;
        } else if (responseTitle.equals("date")) {
            result = numberDate;
        } else {
            result = dateTime;
        }

        return result;
    }

    public static String convert24HourTo12Hour(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the exception as needed
            return null;
        }
    }


}
