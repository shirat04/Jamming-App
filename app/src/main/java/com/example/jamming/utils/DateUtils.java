package com.example.jamming.utils;

import java.text.SimpleDateFormat;
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

}
