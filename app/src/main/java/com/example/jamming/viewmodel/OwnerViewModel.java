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

    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<List<Event>> upcomingEvents = new MutableLiveData<>();
    public MutableLiveData<List<Event>> pastEvents = new MutableLiveData<>();
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


    public void loadOwnerEvents() {
        String uid = authRepo.getCurrentUid();

        if (uid == null) {
            message.setValue("User not logged in");
            return;
        }

        eventRepo.getEventsByOwner(uid)
                .addOnSuccessListener(query -> {

                    List<Event> upcoming = new ArrayList<>();
                    List<Event> past = new ArrayList<>();

                    long now = System.currentTimeMillis();

                    for (QueryDocumentSnapshot doc : query) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        // שורה קריטית: הכנסת ה־ID לאובייקט
                        event.setId(doc.getId());

                        if (event.getDateTime() < now) {
                            past.add(event);
                        } else {
                            upcoming.add(event);
                        }
                    }
                    upcoming.sort((a, b) ->
                            Long.compare(a.getDateTime(), b.getDateTime()));

                    past.sort((a, b) ->
                            Long.compare(b.getDateTime(), a.getDateTime())
                    );

                    upcomingEvents.setValue(upcoming);
                    pastEvents.setValue(past);
                })
                .addOnFailureListener(e ->
                        message.setValue("Failed to load events")
                );
    }

    public void deleteEvent(String eventId) {
        eventRepo.deleteEvent(eventId)
                .addOnSuccessListener(v -> {
                    message.setValue("Event deleted");
                    loadOwnerEvents();
                })
                .addOnFailureListener(e ->
                        message.setValue("Error deleting event")
                );
    }



}
