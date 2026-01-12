package com.example.jamming.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OwnerViewModel extends ViewModel {
    private final AuthRepository authRepo = new AuthRepository();
    private final EventRepository eventRepo = new EventRepository();
    private final UserRepository userRepo = new UserRepository();
    public MutableLiveData<String> ownerName = new MutableLiveData<>();

    public MutableLiveData<List<EventRepository.EventWithId>> events = new MutableLiveData<>();
    public MutableLiveData<String> message = new MutableLiveData<>();

    public void loadOwnerName() {
        String uid = authRepo.getCurrentUid();

        if (uid == null) {
            ownerName.setValue("Owner");
            return;
        }

        userRepo.getUserFullName(uid)
                .addOnSuccessListener(name ->
                        ownerName.setValue(name != null ? name : "Owner")
                )
                .addOnFailureListener(e ->
                        ownerName.setValue("Owner")
                );
    }

    public void logout() {
        authRepo.logout();
    }

    public void loadOwnerEvents() {
        String uid = authRepo.getCurrentUid();

        if (uid == null) {
            message.setValue("User not logged in");
            return;
        }

        eventRepo.getEventsByOwner(uid)
                .addOnSuccessListener(query -> {
                    List<EventRepository.EventWithId> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        list.add(new EventRepository.EventWithId(
                                doc.toObject(Event.class),
                                doc.getId()
                        ));
                    }
                    events.setValue(list);
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to load events")
                );
    }

    public void deleteEvent(String eventId) {
        List<EventRepository.EventWithId> current = events.getValue();
        if (current == null) return;

        List<EventRepository.EventWithId> backup = new ArrayList<>(current);

        List<EventRepository.EventWithId> updated = new ArrayList<>();
        for (EventRepository.EventWithId e : current) {
            if (!e.id.equals(eventId)) {
                updated.add(e);
            }
        }
        events.setValue(updated);

        eventRepo.deleteEvent(eventId)
                .addOnSuccessListener(v ->
                        message.setValue("Event cancelled")
                )
                .addOnFailureListener(e -> {
                    events.setValue(backup);
                    message.setValue("Error cancelling event");
                });
    }



}
