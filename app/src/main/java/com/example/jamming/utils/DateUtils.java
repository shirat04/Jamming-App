package com.example.jamming.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatOnlyDate(long timestamp) {
        return new SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
        ).format(new Date(timestamp));
    }

    public static String formatOnlyTime(long timestamp) {
        return new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(new Date(timestamp));
    }

    public static int minutesFromMidnight(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60
                + c.get(Calendar.MINUTE);
    }

    public static String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    public static String formatDateRange(long start, long end) {
        return formatOnlyDate(start) + " – " + formatOnlyDate(end);
    }

    public static String formatTimeRange(int startMinutes, int endMinutes) {
        return formatMinutes(startMinutes) + " – " + formatMinutes(endMinutes);
    }
}

