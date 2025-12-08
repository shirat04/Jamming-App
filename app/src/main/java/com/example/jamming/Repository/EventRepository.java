package com.example.jamming.Repository;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

public class EventRepository {

    private final FirebaseFirestore db;

    public EventRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    // Create new event in Firestore
    public Task<Void> createEvent(Event event) {

        DocumentReference ref = db.collection("events").document();

        event.setId(ref.getId());

        return ref.set(event);
    }

    // Get an event by ID
    public Task<DocumentSnapshot> getEventById(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get();
    }

    // Get all events in the system
    public Task<QuerySnapshot> getAllEvents() {
        return db.collection("events")
                .get();
    }

    // Get all events created by a specific owner
    public Task<QuerySnapshot> getEventsByOwner(String ownerId) {
        return db.collection("events")
                .whereEqualTo("ownerId", ownerId)
                .get();
    }

    // Update event fields (supports multiple updates)
    public Task<Void> updateEvent(String eventId, Map<String, Object> updates) {
        return db.collection("events")
                .document(eventId)
                .update(updates);
    }

    // Delete an event completely
    public Task<Void> deleteEvent(String eventId) {
        return db.collection("events")
                .document(eventId)
                .delete();
    }

    // Increment reserved seats count
    public Task<Void> incrementReserved(String eventId) {
        return db.collection("events")
                .document(eventId)
                .update("reserved", FieldValue.increment(1));
    }

    // Decrement reserved seats count
    public Task<Void> decrementReserved(String eventId) {
        return db.collection("events")
                .document(eventId)
                .update("reserved", FieldValue.increment(-1));
    }

    // Replace entire event record (useful for cloning)
    public Task<Void> overwriteEvent(String id, Event event) {
        return db.collection("events")
                .document(id)
                .set(event);
    }
}
