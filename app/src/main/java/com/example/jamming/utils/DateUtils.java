package com.example.jamming.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time formatting and conversions.
 * Provides helper methods for displaying and manipulating timestamps.
 */
public class DateUtils {

    /**
     * Formats a timestamp to a date string (day, month, year).
     *
     * @param timestamp Time in milliseconds since epoch
     * @return Formatted date string in the format dd.MM.yyyy
     */
    public static String formatOnlyDate(long timestamp) {
        return new SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
        ).format(new Date(timestamp));
    }

    /**
            * Formats a timestamp to a time string (hours and minutes).
            *
            * @param timestamp Time in milliseconds since epoch
     * @return Formatted time string in the format HH:mm
     */
    public static String formatOnlyTime(long timestamp) {
        return new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(new Date(timestamp));
    }

    /**
     * Converts a timestamp to the number of minutes passed since midnight.
     *
     * @param millis Time in milliseconds since epoch
     * @return Number of minutes from midnight
     */
    public static int minutesFromMidnight(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60
                + c.get(Calendar.MINUTE);
    }

    /**
     * Formats a number of minutes into a time string (HH:mm).
     *
     * @param minutes Total minutes since midnight
     * @return Formatted time string
     */
    public static String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    /**
     * Formats a time range represented by start and end minutes.
     *
     * @param startMinutes Start time in minutes since midnight
     * @param endMinutes End time in minutes since midnight
     * @return Formatted time range string (e.g., "18:00 – 22:00")
     */
    public static String formatTimeRange(int startMinutes, int endMinutes) {
        return formatMinutes(startMinutes) + " – " + formatMinutes(endMinutes);
    }
}

