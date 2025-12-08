package com.example.jamming.Repository;

import com.example.jamming.model.Event;
import com.example.jamming.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    // Get user document by UID
    public Task<DocumentSnapshot> getUserById(String uid) {
        return db.collection("users")
                .document(uid)
                .get();
    }

    // Update a single field of the user document
    public Task<Void> updateUserField(String uid, String fieldName, Object value) {
        return db.collection("users")
                .document(uid)
                .update(fieldName, value);
    }

    // Update multiple user fields at once
    public Task<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        return db.collection("users")
                .document(uid)
                .update(updates);
    }

    // Add an event to the user's registered events list
    public Task<Void> registerEventForUser(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("registeredEventIds", FieldValue.arrayUnion(eventId));
    }

    // Remove an event from the user's registered events list
    public Task<Void> unregisterEventForUser(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("registeredEventIds", FieldValue.arrayRemove(eventId));
    }

    // Get list of event IDs the user is registered to
    public Task<List<String>> getRegisteredEvents(String uid) {
        return db.collection("users")
                .document(uid)
                .get()
                .onSuccessTask(doc -> {

                    if (!doc.exists()) {
                        return Tasks.forException(new Exception("User not found"));
                    }

                    User user = doc.toObject(User.class);

                    List<String> events = user != null && user.getRegisteredEventIds() != null
                            ? user.getRegisteredEventIds() : new ArrayList<>();

                    return Tasks.forResult(events);
                });
    }

    // Update profile image URL for user
    public Task<Void> updateProfileImage(String uid, String imageUrl) {
        return db.collection("users")
                .document(uid)
                .update("profileImageUrl", imageUrl);
    }

    // Update user search radius (for event discovery)
    public Task<Void> updateSearchRadius(String uid, int radiusKm) {
        return db.collection("users")
                .document(uid)
                .update("searchRadiusKm", radiusKm);
    }

    // Update user preferred music types
    public Task<Void> updateFavoriteMusicTypes(String uid, List<String> musicTypes) {
        return db.collection("users")
                .document(uid)
                .update("favoriteMusicTypes", musicTypes);
    }

    // Enable or disable user push notifications
    public Task<Void> updateNotificationsEnabled(String uid, boolean enabled) {
        return db.collection("users")
                .document(uid)
                .update("notificationsEnabled", enabled);
    }

    // Delete entire user document
    public Task<Void> deleteUserProfile(String uid) {
        return db.collection("users")
                .document(uid)
                .delete();
    }

    // Get events filtered by user's music preferences and distance radius
    public Task<List<Event>> getEventsByUserPreferences(User user, double userLat, double userLng) {

        List<String> favMusic = user.getFavoriteMusicTypes();
        int radiusKm = user.getSearchRadiusKm();

        return db.collection("events")
                .whereArrayContainsAny("musicTypes", favMusic)
                .get()
                .continueWithTask(task -> {

                    if (!task.isSuccessful() || task.getResult() == null) {
                        return Tasks.forException(new Exception("Failed to load events"));
                    }

                    List<Event> filteredEvents = new ArrayList<>();

                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {

                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        Double eventLat = doc.getDouble("latitude");
                        Double eventLng = doc.getDouble("longitude");

                        if (eventLat == null || eventLng == null) {
                            continue;
                        }

                        double distance = calculateDistanceKm(userLat, userLng, eventLat, eventLng);

                        // Add event if within allowed radius
                        if (distance <= radiusKm) {
                            filteredEvents.add(event);
                        }
                    }

                    return Tasks.forResult(filteredEvents);
                });
    }

    // Calculate distance between two geo points using Haversine formula
    private double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371; // Earth radius in KM

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                + Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
