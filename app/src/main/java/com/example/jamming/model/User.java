package com.example.jamming.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firebaseId;
    private String fullName;
    private String email;
    private String phone;
    private String username;

    private String profileImageUrl;
    private List<String> registeredEventIds;

    // העדפות - שדות רגילים
    private int searchRadiusKm;
    private List<String> favoriteMusicTypes;
    private boolean notificationsEnabled;
    public User() {
        this.registeredEventIds = new ArrayList<>();
        this.favoriteMusicTypes = new ArrayList<>();
        this.searchRadiusKm = 10;
        this.notificationsEnabled = true;
    }

    // Constructor מלא
    public User(String firebaseId, String fullName, String email, String phone, String username) {
        this.firebaseId = firebaseId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.registeredEventIds = new ArrayList<>();
        this.favoriteMusicTypes = new ArrayList<>();
        this.searchRadiusKm = 10;
        this.notificationsEnabled = true;
    }

    // Getters
    public String getFirebaseId() {
        return firebaseId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
    public String getUsername() { return username;}

    public void setUsername(String username) {this.username = username;}


    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public List<String> getRegisteredEventIds() {
        return registeredEventIds;
    }

    public int getSearchRadiusKm() {
        return searchRadiusKm;
    }

    public List<String> getFavoriteMusicTypes() {
        return favoriteMusicTypes;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    // Setters - חובה ל-Firebase
    public void setFirebaseId(String id) {
        this.firebaseId = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setRegisteredEventIds(List<String> registeredEventIds) {
        this.registeredEventIds = registeredEventIds;
    }

    public void setSearchRadiusKm(int searchRadiusKm) {
        this.searchRadiusKm = searchRadiusKm;
    }

    public void setFavoriteMusicTypes(List<String> favoriteMusicTypes) {
        this.favoriteMusicTypes = favoriteMusicTypes;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void registerToEvent(String eventId) {
        if (!registeredEventIds.contains(eventId)) {
            registeredEventIds.add(eventId);
        }
    }

    public void unregisterFromEvent(String eventId) {
        registeredEventIds.remove(eventId);
    }

    public boolean isRegisteredTo(String eventId) {
        return registeredEventIds.contains(eventId);
    }
}
