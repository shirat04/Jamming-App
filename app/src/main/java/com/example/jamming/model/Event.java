package com.example.jamming.model;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private String id;
    private String ownerId;
    private String name;
    private String description;
    private List<String> musicTypes;
    private String address;
    private long dateTime;
    private int maxCapacity;
    private int reserved;
    private boolean isActive;
    private double latitude;
    private double longitude;


    public Event() {
        this.musicTypes = new ArrayList<>();
    }

    public Event(String ownerId, String name, String description,
                 List<String> musicTypes, String address,
                 long dateTime, int maxCapacity,
                 double latitude, double longitude) {

        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.musicTypes = musicTypes != null ? musicTypes : new ArrayList<>();
        this.address = address;
        this.dateTime = dateTime;
        this.maxCapacity = maxCapacity;
        this.reserved = 0;
        this.isActive = true;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<MusicGenre> getMusicGenresEnum() {
        List<MusicGenre> result = new ArrayList<>();
        if (musicTypes == null) return result;

        for (String s : musicTypes) {
            try {
                result.add(MusicGenre.fromDisplayName(s));
            } catch (Exception ignored) {}
        }
        return result;
    }
    public List<String> getMusicTypes() {
        return musicTypes;
    }

    public String getAddress() { return address; }
    public long getDateTime() { return dateTime; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getReserved() { return reserved; }
    public boolean isActive() { return isActive; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(String id) { this.id = id; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMusicTypes(List<String> musicTypes) { this.musicTypes = musicTypes; }
    public void setAddress(String address) { this.address = address; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }
    public void setActive(boolean active) { isActive = active; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isFull() {return reserved >= maxCapacity;}

    public int getAvailableSpots() {
        return maxCapacity - reserved;
    }

    public boolean canRegister() {return isActive && !isFull();}
}
