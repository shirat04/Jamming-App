package com.example.jamming.model;

public class Event {
    private String id;
    private String ownerId;
    private String name;
    private String artistName;
    private String description;
    private String musicType;
    private String address;
    private long dateTime;
    private int maxCapacity;
    private int reserved;
    private boolean isActive;

    public Event() {}
    public Event(String ownerId, String name, String artistName, String description,
                 String musicType, String address, long dateTime, int maxCapacity) {
        this.ownerId = ownerId;
        this.name = name;
        this.artistName = artistName;
        this.description = description;
        this.musicType = musicType;
        this.address = address;
        this.dateTime = dateTime;
        this.maxCapacity = maxCapacity;
        this.reserved = 0;
        this.isActive = true;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getDescription() {
        return description;
    }

    public String getMusicType() {
        return musicType;
    }

    public String getAddress() {
        return address;
    }

    public long getDateTime() {
        return dateTime;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public int getReserved() {
        return reserved;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setMusicType(String musicType) {
        this.musicType = musicType;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public boolean isFull() {
        return reserved >= maxCapacity;
    }
    public int getAvailableSpots() {
        return maxCapacity - reserved;
    }
    public boolean canRegister() {
        return isActive && !isFull();
    }
}
