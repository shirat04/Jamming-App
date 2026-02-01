package com.example.jamming.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a set of filtering criteria for events.
 * This object is used to store user-selected filters and is later
 * applied by the EventFilterEngine.
 */
public class EventFilter {

    /** Selected music genres (no duplicates expected) */
    private List<MusicGenre> musicTypes = new ArrayList<>();

    /** Location filter: center coordinates and radius in kilometers */
    private Double centerLat;
    private Double centerLng;
    private Integer radiusKm;

    /** Time range filter in minutes from midnight (00:00) */
    private Integer startMinute;
    private Integer endMinute;

    /** Date range filter represented as timestamps in milliseconds */
    private Long startDateMillis;
    private Long endDateMillis;

    /** Available spots range filter */
    private Integer minAvailableSpots;
    private Integer maxAvailableSpots;

    /** Event capacity range filter */
    private Integer minCapacity;
    private Integer maxCapacity;

    /* ===== Getters / Setters ===== */

    /**
     * Returns the selected music genres.
     *
     * @return List of selected MusicGenre values
     */
    public List<MusicGenre> getMusicTypes() {
        return musicTypes;
    }

    /**
     * Sets the selected music genres.
     *
     * @param musicTypes List of MusicGenre values
     */
    public void setMusicTypes(List<MusicGenre> musicTypes) {
        this.musicTypes = musicTypes;
    }

    /**
     * Sets the location-based filtering parameters.
     *
     * @param lat Center latitude
     * @param lng Center longitude
     * @param radiusKm Search radius in kilometers
     */
    public void setLocation(Double lat, Double lng, Integer radiusKm) {
        this.centerLat = lat;
        this.centerLng = lng;
        this.radiusKm = radiusKm;
    }

    public Double getCenterLat() { return centerLat; }
    public Double getCenterLng() { return centerLng; }
    public Integer getRadiusKm() { return radiusKm; }

    /**
     * Sets the allowed time range for events.
     * The range is represented in minutes from midnight.
     *
     * @param startMinute Start time in minutes
     * @param endMinute End time in minutes
     */
    public void setTimeRange(Integer startMinute, Integer endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }


    public Integer getStartMinute() { return startMinute; }
    public Integer getEndMinute() { return endMinute; }

    /**
     * Sets the allowed date range for events.
     *
     * @param start Start timestamp (milliseconds)
     * @param end End timestamp (milliseconds)
     */
    public void setDateRange(Long start, Long end) {
        this.startDateMillis = start;
        this.endDateMillis = end;
    }


    public Long getStartDateMillis() { return startDateMillis; }
    public Long getEndDateMillis() { return endDateMillis; }

    /**
     * Sets the allowed range of available spots.
     *
     * @param min Minimum number of available spots
     * @param max Maximum number of available spots
     */
    public void setAvailableSpotsRange(Integer min, Integer max) {
        this.minAvailableSpots = min;
        this.maxAvailableSpots = max;
    }

    public Integer getMinAvailableSpots() { return minAvailableSpots; }
    public Integer getMaxAvailableSpots() { return maxAvailableSpots; }

    /**
     * Sets the allowed range for event capacity.
     *
     * @param min Minimum capacity
     * @param max Maximum capacity
     */
    public void setCapacityRange(Integer min, Integer max) {
        this.minCapacity = min;
        this.maxCapacity = max;
    }
    public Integer getMinCapacity() { return minCapacity; }
    public Integer getMaxCapacity() { return maxCapacity; }

}
