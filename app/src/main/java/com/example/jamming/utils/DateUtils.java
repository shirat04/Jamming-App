package com.example.jamming.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf =
                new SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
