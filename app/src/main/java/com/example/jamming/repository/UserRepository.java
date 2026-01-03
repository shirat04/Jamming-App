package com.example.jamming.repository;

import com.example.jamming.model.Event;
import com.example.jamming.model.User;
import com.example.jamming.utils.GeoUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get user document by UID
    public Task<DocumentSnapshot> getUserById(String uid) {
        return db.collection("users")
                .document(uid)
                .get();
    }
    public Task<String> getUserFullName(String uid) {
        return getUserById(uid).continueWith(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }

            DocumentSnapshot doc = task.getResult();
            return doc.exists() ? doc.getString("fullName") : null;
        });
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

                        double distance = GeoUtils.calculateDistanceKm(userLat, userLng, eventLat, eventLng);

                        // Add event if within allowed radius
                        if (distance <= radiusKm) {
                            filteredEvents.add(event);
                        }
                    }

                    return Tasks.forResult(filteredEvents);
                });
    }


}
