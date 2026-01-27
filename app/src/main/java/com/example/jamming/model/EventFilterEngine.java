package com.example.jamming.model;

import com.example.jamming.model.Event;
import com.example.jamming.model.EventFilter;
import com.example.jamming.utils.GeoUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventFilterEngine {

    public static List<Event> filter(
            List<Event> events,
            EventFilter filter
    ) {
        if (filter == null) return events;

        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (matches(e, filter)) {
                result.add(e);
            }
        }
        return result;
    }

    private static boolean matches(Event e, EventFilter f) {

        if (!e.isActive() || !e.canRegister()) {
            return false;
        }

        // 🎵 Music types
        if (!f.getMusicTypes().isEmpty()) {
            boolean match = false;
            for (String genreStr : e.getMusicTypes()) {
                try {
                    MusicGenre genreEnum = MusicGenre.fromDisplayName(genreStr);

                    if (f.getMusicTypes().contains(genreEnum)) {
                        match = true;
                        break;
                    }

                } catch (IllegalArgumentException ignored) {
                }
            }

            if (!match) return false;
        }

        // 📍 Location
        if (f.getRadiusKm() != null) {
            double dist = GeoUtils.calculateDistanceKm(
                    f.getCenterLat(),
                    f.getCenterLng(),
                    e.getLatitude(),
                    e.getLongitude()
            );
            if (dist > f.getRadiusKm()) return false;
        }

        // 📅 Date range
        if (f.getStartDateMillis() != null && f.getEndDateMillis() != null) {
            long t = e.getDateTime();
            if (t < f.getStartDateMillis() || t > f.getEndDateMillis())
                return false;
        }

        // ⏰ Time range
        if (f.getStartMinute() != null && f.getEndMinute() != null) {
            int minutes = minutesFromMidnight(e.getDateTime());
            if (minutes < f.getStartMinute() || minutes > f.getEndMinute())
                return false;
        }

        // 👥 Available spots
        int available = e.getAvailableSpots();

        if (f.getMinAvailableSpots() != null
                && available < f.getMinAvailableSpots())
            return false;

        if (f.getMaxAvailableSpots() != null
                && available > f.getMaxAvailableSpots())
            return false;

        return true;
    }

    private static int minutesFromMidnight(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.HOUR_OF_DAY) * 60
                + c.get(Calendar.MINUTE);
    }
}