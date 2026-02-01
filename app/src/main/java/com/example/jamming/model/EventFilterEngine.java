package com.example.jamming.model;

import com.example.jamming.utils.DateUtils;
import com.example.jamming.utils.GeoUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Engine responsible for filtering events based on a given EventFilter.
 * Applies multiple criteria such as activity status, genre, location,
 * date range, time range, and capacity constraints.
 */
public class EventFilterEngine {

    /**
     * Filters a list of events according to the provided filter.
     *
     * @param events List of events to filter
     * @param filter EventFilter containing filtering criteria
     * @return List of events that match the filter
     */
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

    /**
     * Checks whether a single event matches all filter conditions.
     *
     * @param event Event to check
     * @param eventFilter Filtering criteria
     * @return True if the event matches all conditions, false otherwise
     */
    private static boolean matches(Event event, EventFilter eventFilter) {

        // Ignore inactive events
        if (!event.isActive()) {
            return false;
        }

        // Ignore events that already occurred
        long now = System.currentTimeMillis();
        if (event.getDateTime() < now) {
            return false;
        }

        // Filter by music genres
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
                    // Ignore unknown or unsupported genre values
                }
            }

            if (!match) return false;
        }

        // Filter by geographic distance
        if (eventFilter.getRadiusKm() != null) {
            double dist = GeoUtils.calculateDistanceKm(
                    eventFilter.getCenterLat(),
                    eventFilter.getCenterLng(),
                    event.getLatitude(),
                    event.getLongitude()
            );
            if (dist > eventFilter.getRadiusKm()) return false;
        }

        // Filter by date range
        if (eventFilter.getStartDateMillis() != null && eventFilter.getEndDateMillis() != null) {
            long t = event.getDateTime();
            if (t < eventFilter.getStartDateMillis() || t > eventFilter.getEndDateMillis())
                return false;
        }

        // Filter by time range (supports overnight ranges)
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


        // Filter by available spots
        int available = event.getAvailableSpots();

        if (eventFilter.getMinAvailableSpots() != null
                && available < eventFilter.getMinAvailableSpots())
            return false;

        if (eventFilter.getMaxAvailableSpots() != null
                && available > eventFilter.getMaxAvailableSpots())
            return false;

        // Filter by event capacity
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