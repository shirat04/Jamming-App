package com.example.jamming.repository;

import com.example.jamming.model.EventFilter;
import com.example.jamming.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository responsible for managing user-related data.
 * Handles all interactions with the "users" collection in Firestore.
 * This class follows the Repository pattern and serves as an abstraction
 * layer between the ViewModel and the data source.
 */
public class UserRepository {

    private final FirebaseFirestore db;

    // Default constructor using the Firestore singleton instance.
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor for dependency injection (used mainly for testing).
     * @param db Firestore instance
     */
    public UserRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Retrieves a user document by its unique identifier (UID).
     *
     * @param uid User ID
     * @return Task containing the user document snapshot
     */    public Task<DocumentSnapshot> getUserById(String uid) {
        return db.collection("users")
                .document(uid)
                .get();
    }

    /**
     * Retrieves the full name of a user.
     *
     * @param uid User ID
     * @return Task containing the user's full name, or null if not found
     */
    public Task<String> getUserFullName(String uid) {
        return getUserById(uid).continueWith(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }

            DocumentSnapshot doc = task.getResult();
            return doc.exists() ? doc.getString("fullName") : null;
        });
    }


    /**
     * Updates a single field in the user's document.
     *
     * @param uid User ID
     * @param fieldName Name of the field to update
     * @param value New value for the field
     * @return Task representing the update operation
     */
    public Task<Void> updateUserField(String uid, String fieldName, Object value) {
        return db.collection("users")
                .document(uid)
                .update(fieldName, value);
    }

    /**
     * Updates multiple fields in the user's profile at once.
     *
     * @param uid User ID
     * @param updates Map containing field names and their new values
     * @return Task representing the update operation
     */
    public Task<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        return db.collection("users")
                .document(uid)
                .update(updates);
    }
    /**
     * Retrieves the last event filter used by the user.
     *
     * @param uid User ID
     * @return Task containing the last used EventFilter, or null if not available
     */
    public Task<EventFilter> getLastEventFilter(String uid) {
        return getUserById(uid).continueWith(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }

            User user = task.getResult().toObject(User.class);
            return user != null ? user.getLastEventFilter() : null;
        });
    }
    /**
     * Saves the user's last used event filter.
     *
     * @param uid User ID
     * @param filter Event filter to save
     */
    public void saveLastEventFilter(String uid, EventFilter filter) {
        updateUserField(uid, "lastEventFilter", filter);
    }


    /**
     * Registers a user to an event by adding the event ID
     * to the user's registered events list.
     *
     * @param uid User ID
     * @param eventId Event ID to register
     * @return Task representing the update operation
     */    public Task<Void> registerEventForUser(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("registeredEventIds", FieldValue.arrayUnion(eventId));
    }

    /**
     * Unregisters a user from an event by removing the event ID
     * from the user's registered events list.
     *
     * @param uid User ID
     * @param eventId Event ID to unregister
     * @return Task representing the update operation
     */
    public Task<Void> unregisterEventForUser(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("registeredEventIds", FieldValue.arrayRemove(eventId));
    }

    /**
     * Retrieves the list of event IDs the user is registered to.
     *
     * @param uid User ID
     * @return Task containing a list of event IDs
     */
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

    /**
     * Updates the user's profile image URL.
     *
     * @param uid User ID
     * @param imageUrl URL of the new profile image
     * @return Task representing the update operation
     */
    public Task<Void> updateProfileImage(String uid, String imageUrl) {
        return db.collection("users")
                .document(uid)
                .update("profileImageUrl", imageUrl);
    }

    /**
     * Enables or disables push notifications for the user.
     *
     * @param uid User ID
     * @param enabled Whether notifications are enabled
     * @return Task representing the update operation
     */
    public Task<Void> updateNotificationsEnabled(String uid, boolean enabled) {
        return db.collection("users")
                .document(uid)
                .update("notificationsEnabled", enabled);
    }

    /**
     * Deletes the entire user profile document from Firestore.
     *
     * @param uid User ID
     * @return Task representing the delete operation
     */
    public Task<Void> deleteUserProfile(String uid) {
        return db.collection("users")
                .document(uid)
                .delete();
    }

}
