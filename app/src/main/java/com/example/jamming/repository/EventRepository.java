package com.example.jamming.repository;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventRepository {
    public static class EventWithId {
        public Event event;
        public String id;

        public EventWithId(Event event, String id) {
            this.event = event;
            this.id = id;
        }
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


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
    public Task<List<Event>> getActiveEvents() {
        return FirebaseFirestore.getInstance()
                .collection("events")
                .whereEqualTo("active", true)
                .get()
                .continueWith(task -> {
                    List<Event> list = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            e.setId(doc.getId());
                            list.add(e);
                        }
                    }
                    return list;
                });
    }

    // Decrement reserved seats count
    public Task<Void> decrementReserved(String eventId) {
        return db.collection("events")
                .document(eventId)
                .update("reserved", FieldValue.increment(-1));
    }

    public Task<Event> getEventByIdAsEvent(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task ->
                        task.getResult().toObject(Event.class)
                );
    }

}
