package com.example.jamming.model;


import java.util.ArrayList;
import java.util.List;

public class Owner extends User {

    private String locationName;
    private String address;
    private String city;
    private String businessDescription;
    private String logoImageUrl;
    private List<String> ownedEventIds;
    private double latitude;
    private double longitude;

    // Constructor ריק - חובה ל-Firebase
    public Owner() {
        super();
        this.ownedEventIds = new ArrayList<>();
    }

    // Constructor מלא
    public Owner(String id, String fullName, String email, String phone,
                 String locationName, String address, String city) {
        super(id, fullName, email, phone);
        this.locationName = locationName;
        this.address = address;
        this.city = city;
        this.ownedEventIds = new ArrayList<>();
    }

    // Getters
    public String getLocationName() {
        return locationName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public String getLogoImageUrl() {
        return logoImageUrl;
    }

    public List<String> getOwnedEventIds() {
        return ownedEventIds;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters - חובה ל-Firebase
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public void setLogoImageUrl(String logoImageUrl) {
        this.logoImageUrl = logoImageUrl;
    }

    public void setOwnedEventIds(List<String> ownedEventIds) {
        this.ownedEventIds = ownedEventIds;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Methods
    public void addEvent(String eventId) {
        if (!ownedEventIds.contains(eventId)) {
            ownedEventIds.add(eventId);
        }
    }

    public void removeEvent(String eventId) {
        ownedEventIds.remove(eventId);
    }

    public int getEventsCount() {
        return ownedEventIds.size();
    }

    public String getFullAddress() {
        return address + ", " + city;
    }
}