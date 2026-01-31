package com.example.jamming.model;

import com.example.jamming.utils.DateUtils;
import com.example.jamming.utils.GeoUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventFilterEngine {



    public static List<Event> filter(List<Event> events, EventFilter filter) {
        if (filter == null) return events;

        List<Event> result = new ArrayList<>();
        for (Event event : events) {
            if (matches(event, filter)) {
                result.add(event);
            }
        }
        return result;
    }

    private static boolean matches(Event event, EventFilter eventFilter) {

        if (!event.isActive()) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (event.getDateTime() < now) {
            return false;
        }


        //  Music types
        if (!eventFilter.getMusicTypes().isEmpty()) {
            boolean match = false;
            for (String genreStr : event.getMusicTypes()) {
                try {
                    MusicGenre genreEnum = MusicGenre.fromDisplayName(genreStr);

                    if (eventFilter.getMusicTypes().contains(genreEnum)) {
                        match = true;
                        break;
                    }

                } catch (IllegalArgumentException ignored) {
                }
            }

            if (!match) return false;
        }

        // Location
        if (eventFilter.getRadiusKm() != null) {
            double dist = GeoUtils.calculateDistanceKm(
                    eventFilter.getCenterLat(),
                    eventFilter.getCenterLng(),
                    event.getLatitude(),
                    event.getLongitude()
            );
            if (dist > eventFilter.getRadiusKm()) return false;
        }

        // Date range
        if (eventFilter.getStartDateMillis() != null && eventFilter.getEndDateMillis() != null) {
            long t = event.getDateTime();
            if (t < eventFilter.getStartDateMillis() || t > eventFilter.getEndDateMillis())
                return false;
        }

        // Time range (supports overnight ranges)
        if (eventFilter.getStartMinute() != null && eventFilter.getEndMinute() != null) {
            int minutes = DateUtils.minutesFromMidnight(event.getDateTime());

            int start = eventFilter.getStartMinute();
            int end   = eventFilter.getEndMinute();

            boolean matchesTime;

            if (start <= end) {
                // Same-day range (e.g. 10:00–18:00)
                matchesTime = minutes >= start && minutes <= end;
            } else {
                // Overnight range (e.g. 23:00–03:00)
                matchesTime = minutes >= start || minutes <= end;
            }

            if (!matchesTime) return false;
        }


        // Available spots
        int available = event.getAvailableSpots();

        if (eventFilter.getMinAvailableSpots() != null
                && available < eventFilter.getMinAvailableSpots())
            return false;

        if (eventFilter.getMaxAvailableSpots() != null
                && available > eventFilter.getMaxAvailableSpots())
            return false;

        // Event size (maximum capacity)
        int capacity = event.getMaxCapacity();

        if (eventFilter.getMinCapacity() != null
                && capacity < eventFilter.getMinCapacity())
            return false;

        if (eventFilter.getMaxCapacity() != null
                && capacity > eventFilter.getMaxCapacity())
            return false;

        return true;

    }


}