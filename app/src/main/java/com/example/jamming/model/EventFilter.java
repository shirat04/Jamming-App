package com.example.jamming.model;

import java.util.HashSet;
import java.util.Set;

public class EventFilter {

    // Genres
    private Set<String> musicTypes = new HashSet<>();

    // Location
    private Double centerLat;
    private Double centerLng;
    private Integer radiusKm;

    // Time (in minutes from 00:00)
    private Integer startMinute;
    private Integer endMinute;

    // Date (timestamp millis)
    private Long startDateMillis;
    private Long endDateMillis;

    // Capacity
    private Integer minAvailableSpots;
    private Integer maxAvailableSpots;

    /* ===== Getters / Setters ===== */

    public Set<String> getMusicTypes() {
        return musicTypes;
    }

    public void setMusicTypes(Set<String> musicTypes) {
        this.musicTypes = musicTypes;
    }

    public void setLocation(Double lat, Double lng, Integer radiusKm) {
        this.centerLat = lat;
        this.centerLng = lng;
        this.radiusKm = radiusKm;
    }

    public Double getCenterLat() { return centerLat; }
    public Double getCenterLng() { return centerLng; }
    public Integer getRadiusKm() { return radiusKm; }

    public void setTimeRange(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public Integer getStartMinute() { return startMinute; }
    public Integer getEndMinute() { return endMinute; }

    public void setDateRange(long start, long end) {
        this.startDateMillis = start;
        this.endDateMillis = end;
    }

    public Long getStartDateMillis() { return startDateMillis; }
    public Long getEndDateMillis() { return endDateMillis; }

    public void setAvailableSpotsRange(Integer min, Integer max) {
        this.minAvailableSpots = min;
        this.maxAvailableSpots = max;
    }

    public Integer getMinAvailableSpots() { return minAvailableSpots; }
    public Integer getMaxAvailableSpots() { return maxAvailableSpots; }
}
