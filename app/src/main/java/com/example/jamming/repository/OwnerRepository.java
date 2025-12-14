package com.example.jamming.repository;

import com.example.jamming.model.Owner;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OwnerRepository {

    private final FirebaseFirestore db;

    public OwnerRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    // Get owner document by UID
    public Task<DocumentSnapshot> getOwnerById(String uid) {
        return db.collection("owners")
                .document(uid)
                .get();
    }

    // Update a single field of the owner document
    public Task<Void> updateOwnerField(String uid, String field, Object value) {
        return db.collection("owners")
                .document(uid)
                .update(field, value);
    }

    // Update multiple owner fields at once
    public Task<Void> updateOwnerProfile(String uid, Map<String, Object> updates) {
        return db.collection("owners")
                .document(uid)
                .update(updates);
    }

    // Update business logo image URL
    public Task<Void> updateLogo(String uid, String logoUrl) {
        return db.collection("owners")
                .document(uid)
                .update("logoImageUrl", logoUrl);
    }

    // Add a new event ID to the owner's event list
    public Task<Void> addOwnedEvent(String uid, String eventId) {
        return db.collection("owners")
                .document(uid)
                .update("ownedEventIds", FieldValue.arrayUnion(eventId));
    }

    // Remove event ID from owner's event list
    public Task<Void> removeOwnedEvent(String uid, String eventId) {
        return db.collection("owners")
                .document(uid)
                .update("ownedEventIds", FieldValue.arrayRemove(eventId));
    }

    // Get list of event IDs that belong to the owner
    public Task<List<String>> getOwnedEvents(String uid) {
        return getOwnerById(uid).onSuccessTask(doc -> {

            if (!doc.exists()) {
                return Tasks.forException(new Exception("Owner not found"));
            }

            Owner owner = doc.toObject(Owner.class);

            List<String> events = owner != null && owner.getOwnedEventIds() != null
                    ? owner.getOwnedEventIds()
                    : new java.util.ArrayList<>();

            return Tasks.forResult(events);
        });
    }

    // Delete the entire owner profile
    public Task<Void> deleteOwnerProfile(String uid) {
        return db.collection("owners")
                .document(uid)
                .delete();
    }
}
