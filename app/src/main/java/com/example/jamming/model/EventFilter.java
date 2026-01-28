package com.example.jamming.model;

import java.util.HashSet;
import java.util.Set;

public class EventFilter {

    // Selected music genres (no duplicates)
    private Set<MusicGenre> musicTypes = new HashSet<>();

    // Location filter (center point + radius)
    private Double centerLat;
    private Double centerLng;
    private Integer radiusKm;

    // Time range in minutes from midnight (00:00)
    private Integer startMinute;
    private Integer endMinute;

    // Date range in milliseconds (timestamps)
    private Long startDateMillis;
    private Long endDateMillis;

    // Available spots range
    private Integer minAvailableSpots;
    private Integer maxAvailableSpots;

    /* ===== Getters / Setters ===== */

    // Returns selected music genres
    public Set<MusicGenre> getMusicTypes() {
        return musicTypes;
    }

    // Sets selected music genres
    public void setMusicTypes(Set<MusicGenre> musicTypes) {
        this.musicTypes = musicTypes;
    }

    // Sets location filter (center + radius)
    public void setLocation(Double lat, Double lng, Integer radiusKm) {
        this.centerLat = lat;
        this.centerLng = lng;
        this.radiusKm = radiusKm;
    }

    public Double getCenterLat() { return centerLat; }
    public Double getCenterLng() { return centerLng; }
    public Integer getRadiusKm() { return radiusKm; }

    // Sets allowed time range (in minutes)
    public void setTimeRange(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public Integer getStartMinute() { return startMinute; }
    public Integer getEndMinute() { return endMinute; }

    // Sets allowed date range (timestamps)
    public void setDateRange(long start, long end) {
        this.startDateMillis = start;
        this.endDateMillis = end;
    }

    public Long getStartDateMillis() { return startDateMillis; }
    public Long getEndDateMillis() { return endDateMillis; }

    // Sets allowed range of available spots
    public void setAvailableSpotsRange(Integer min, Integer max) {
        this.minAvailableSpots = min;
        this.maxAvailableSpots = max;
    }

    public Integer getMinAvailableSpots() { return minAvailableSpots; }
    public Integer getMaxAvailableSpots() { return maxAvailableSpots; }
}
