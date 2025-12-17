package com.example.jamming.repository;

import com.example.jamming.model.Owner;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class OwnerRepository {

    private final FirebaseFirestore db;

    public OwnerRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    // Add a new event ID to the owner's event list
    public Task<Void> addOwnedEvent(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("ownedEventIds", FieldValue.arrayUnion(eventId));
    }

    // Remove event ID from owner's event list
    public Task<Void> removeOwnedEvent(String uid, String eventId) {
        return db.collection("users")
                .document(uid)
                .update("ownedEventIds", FieldValue.arrayRemove(eventId));
    }

    // Get list of event IDs that belong to the owner
    public Task<List<String>> getOwnedEvents(String uid) {
            return db.collection("users")
                    .document(uid)
                    .get()
                    .onSuccessTask(doc -> {
                        Owner owner = doc.toObject(Owner.class);
                        String type = doc.getString("userType");
                        if (!"owner".equals(type)) {
                            return Tasks.forException(new Exception("User is not an owner"));
                        }

                        List<String> events =
                                owner != null && owner.getOwnedEventIds() != null
                                        ? owner.getOwnedEventIds()
                                        : new ArrayList<>();
                        return Tasks.forResult(events);
                    });
    }

}
