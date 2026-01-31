package com.example.jamming.repository;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventRepository {
    public static class EventWithId {
        public Event event;
        public String id;

        public EventWithId(Event event, String id) {
            this.event = event;
            this.id = id;
        }
    }

    private final FirebaseFirestore db;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public EventRepository(FirebaseFirestore db) {
        this.db = db;
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
    public Task<QuerySnapshot> getEventsByIds(List<String> ids) {
        return db.collection("events")
                .whereIn(FieldPath.documentId(), ids)
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

    public Task<List<Event>> getActiveEvents() {
        return db.collection("events")
                .whereEqualTo("active", true)
                .get()
                .continueWith(task -> {
                    List<Event> list = new ArrayList<>();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : task.getResult()) {
                        Event e = doc.toObject(Event.class);
                        if (e == null) continue;

                        e.setId(doc.getId());

                        if (e.getDateTime() < now) {
                            db.collection("events")
                                    .document(e.getId())
                                    .update("active", false);
                            continue;
                        }
                        list.add(e);
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

    public void registerUserIfCapacityAvailable(
            String eventId,
            String uid,
            Runnable onSuccess,
            Consumer<String> onError
    ) {
        db.runTransaction(transaction -> {

                    DocumentReference eventRef =
                            db.collection("events").document(eventId);
                    DocumentReference userRef =
                            db.collection("users").document(uid);

                    DocumentSnapshot eventSnap = transaction.get(eventRef);

                    long reserved = eventSnap.getLong("reserved");
                    long max = eventSnap.getLong("maxCapacity");

                    if (reserved >= max) {
                        throw new RuntimeException("EVENT_FULL");
                    }

                    transaction.update(eventRef,
                            "reserved", FieldValue.increment(1));

                    transaction.update(userRef,
                            "registeredEventIds", FieldValue.arrayUnion(eventId));

                    return null;
                }).addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> {
                    if ("EVENT_FULL".equals(e.getMessage())) {
                        onError.accept("האירוע מלא – לא ניתן להירשם");
                    } else {
                        onError.accept("שגיאה בהרשמה");
                    }
                });
    }


}
