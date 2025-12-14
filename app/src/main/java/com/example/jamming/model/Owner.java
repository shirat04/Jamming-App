package com.example.jamming.model;


import java.util.ArrayList;
import java.util.List;

public class Owner extends User {

    private String businessDescription;
    private List<String> ownedEventIds;

    // Constructor ריק - חובה ל-Firebase
    public Owner() {
        super();
        this.ownedEventIds = new ArrayList<>();
    }

    // Constructor מלא
    public Owner(String id, String fullName, String email, String phone, String username,
                 String locationName, String address, String city) {
        super(id, fullName, email, phone, username);;
        this.ownedEventIds = new ArrayList<>();
    }

    // Getters

    public String getBusinessDescription() {
        return businessDescription;
    }


    public List<String> getOwnedEventIds() {
        return ownedEventIds;
    }


    // Setters - חובה ל-Firebase

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }


    public void setOwnedEventIds(List<String> ownedEventIds) {
        this.ownedEventIds = ownedEventIds;
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

}