package com.example.jamming.repository;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository responsible for managing event-related data.
 * Handles all interactions with the "events" collection in Firestore.
 * This class follows the Repository pattern and abstracts data access
 * from higher application layers (e.g., ViewModels).
 */
public class EventRepository {

    private final FirebaseFirestore db;

    /**
     * Default constructor using the Firestore singleton instance.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor for dependency injection (mainly used for testing).
     *
     * @param db Firestore instance
     */
    public EventRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Creates a new event document in Firestore.
     * A unique document ID is generated and assigned to the event.
     *
     * @param event Event object to store
     * @return Task representing the create operation
     */
    public Task<Void> createEvent(Event event) {

        DocumentReference ref = db.collection("events").document();

        // Assign generated Firestore ID to the event object
        event.setId(ref.getId());

        return ref.set(event);
    }

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventId Event ID
     * @return Task containing the event document snapshot
     */
    public Task<DocumentSnapshot> getEventById(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get();
    }

    /**
     * Retrieves multiple events by their document IDs.
     *
     * @param ids List of event IDs
     * @return Task containing a snapshot of matching events
     */
    public Task<QuerySnapshot> getEventsByIds(List<String> ids) {
        return db.collection("events")
                .whereIn(FieldPath.documentId(), ids)
                .get();
    }


    /**
     * Retrieves all events created by a specific owner.
     *
     * @param ownerId Owner user ID
     * @return Task containing a snapshot of matching events
     */
    public Task<QuerySnapshot> getEventsByOwner(String ownerId) {
        return db.collection("events")
                .whereEqualTo("ownerId", ownerId)
                .get();
    }

    /**
     * Updates one or more fields of an existing event.
     *
     * @param eventId Event ID
     * @param updates Map of field names and new values
     * @return Task representing the update operation
     */
    public Task<Void> updateEvent(String eventId, Map<String, Object> updates) {
        return db.collection("events")
                .document(eventId)
                .update(updates);
    }

    /**
     * Deletes an event document from Firestore.
     *
     * @param eventId Event ID
     * @return Task representing the delete operation
     */
    public Task<Void> deleteEvent(String eventId) {
        return db.collection("events")
                .document(eventId)
                .delete();
    }

    /**
     * Retrieves all active events.
     * Events whose date has already passed are automatically marked as inactive.
     *
     * @return Task containing a list of currently active events
     */
    public Task<List<Event>> getActiveEvents() {
        return db.collection("events")
                .whereEqualTo("active", true)
                .get()
                .continueWith(task -> {
                    List<Event> list = new ArrayList<>();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : task.getResult()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        event.setId(doc.getId());

                        if (event.getDateTime() < now) {
                            db.collection("events")
                                    .document(event.getId())
                                    .update("active", false);
                            continue;
                        }
                        list.add(event);
                    }
                    return list;
                });
    }

    /**
     * Retrieves all events created by a specific owner.
     *
     * Each event is mapped from Firestore into an {@link Event} object,
     * including its document ID.
     *
     * @param ownerId Owner user ID
     * @return Task containing a list of the owner's events
     */
    public Task<List<Event>> getOwnerEventsMapped(String ownerId) {
        return db.collection("events")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .continueWith(task -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;
                        event.setId(doc.getId());
                        events.add(event);
                    }
                    return events;
                });
    }


    /**
     * Decrements the number of reserved seats for an event.
     *
     * @param eventId Event ID
     * @return Task representing the update operation
     */
    public Task<Void> decrementReserved(String eventId) {
        return db.collection("events")
                .document(eventId)
                .update("reserved", FieldValue.increment(-1));
    }

    /**
     * Registers a user to an event only if capacity is available.
     * This operation is performed atomically using a Firestore transaction.
     *
     * @param eventId Event ID
     * @param uid User ID
     * @return Task representing the transactional operation
     */
    public Task<Void> registerUserIfCapacityAvailable(String eventId, String uid) {

        return db.runTransaction(transaction -> {

            DocumentReference eventRef =
                    db.collection("events").document(eventId);
            DocumentReference userRef =
                    db.collection("users").document(uid);

            DocumentSnapshot eventSnap = transaction.get(eventRef);

            if (!eventSnap.exists()) {
                throw new RuntimeException("EVENT_NOT_FOUND");
            }

            Long reserved = eventSnap.getLong("reserved");
            Long max = eventSnap.getLong("maxCapacity");

            if (reserved == null || max == null) {
                throw new RuntimeException("INVALID_EVENT_DATA");
            }

            if (reserved >= max) {
                throw new RuntimeException("EVENT_FULL");
            }

            // update num reserved
            transaction.update(eventRef, "reserved", FieldValue.increment(1));

            // update num available
            transaction.update(eventRef, "availableSpots", FieldValue.increment(-1));


            //update participants list
            transaction.update(eventRef, "participants", FieldValue.arrayUnion(uid));

            // update user's registered events
            transaction.update(userRef, "registeredEventIds", FieldValue.arrayUnion(eventId));

            return null;
        });
    }

    //interface on event change listener
    public interface OnEventChangeListener {
        void onEventChanged(String title, String message);
    }

    // util func to save notification to history
    private void saveNotificationToHistory(String userId, String title, String message) {
        java.util.Map<String, Object> notifMap = new java.util.HashMap<>();
        notifMap.put("title", title);
        notifMap.put("message", message);
        notifMap.put("timestamp", System.currentTimeMillis());

        // add notifMap to user's notifications on db
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notifMap);
    }

    public void listenToUserEvents(String userId, OnEventChangeListener listener) {
        db.collection("events")
                .whereArrayContains("participants", userId) // מאזין רק לאירועים שנרשמתי אליהם
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Event event = dc.getDocument().toObject(Event.class);
                        String title = "";
                        String message = "";
                        boolean notify = false;

                        // זיהוי שינוי
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            title = "Event Update";
                            message = "The event '" + event.getName() + "' details have changed.";
                            notify = true;
                        }
                        // זיהוי ביטול
                        else if (dc.getType() == DocumentChange.Type.REMOVED) {
                            title = "Event Cancelled";
                            message = "The event '" + event.getName() + "' was cancelled.";
                            notify = true;
                        }

                        if (notify) {
                            // message on phone
                            listener.onEventChanged(title, message);

                            // add notification in db
                            saveNotificationToHistory(userId, title, message);
                        }
                    }
                });
    }


}
