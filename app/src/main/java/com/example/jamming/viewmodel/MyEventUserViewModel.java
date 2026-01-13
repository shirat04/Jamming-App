package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class MyEventUserViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final EventRepository eventRepository = new EventRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final MutableLiveData<List<EventWithId>> myEvents = new MutableLiveData<>();
    private final MutableLiveData<String> emptyMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<EventWithId>> getMyEvents() {
        return myEvents;
    }

    public LiveData<String> getEmptyMessage() {
        return emptyMessage;
    }

    public LiveData<Boolean> getCancelSuccess() {
        return cancelSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // עטיפה קטנה לאירוע + id
    public static class EventWithId {
        public final String id;
        public final Event event;

        public EventWithId(String id, Event event) {
            this.id = id;
            this.event = event;
        }
    }

    public void loadMyEvents() {
        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            emptyMessage.postValue("משתמש לא מחובר");
            return;
        }

        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(ids -> {
                    if (ids == null || ids.isEmpty()) {
                        emptyMessage.postValue("עוד לא נרשמת לאירועים 🙂");
                        myEvents.postValue(new ArrayList<>());
                        return;
                    }

                    loadEventsByIds(ids);
                })
                .addOnFailureListener(e ->
                        errorMessage.postValue("שגיאה בטעינת My Events")
                );
    }

    private void loadEventsByIds(List<String> ids) {
        List<EventWithId> result = new ArrayList<>();

        // whereIn מוגבל ל־10 → chunking
        for (int i = 0; i < ids.size(); i += 10) {
            List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));

            eventRepository.getEventsByIds(chunk)
                    .addOnSuccessListener(snapshot -> {
                        snapshot.getDocuments().forEach(doc -> {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                result.add(new EventWithId(doc.getId(), event));
                            }
                        });
                        myEvents.postValue(result);
                    })
                    .addOnFailureListener(e ->
                            errorMessage.postValue("שגיאה בטעינת אירועים")
                    );
        }
    }

    public void cancelRegistration(String eventId) {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;

        userRepository.unregisterEventForUser(uid, eventId)
                .addOnSuccessListener(unused ->
                        eventRepository.decrementReserved(eventId)
                                .addOnSuccessListener(u -> {
                                    cancelSuccess.postValue(true);
                                    loadMyEvents(); // ריענון
                                })
                )
                .addOnFailureListener(e ->
                        errorMessage.postValue("שגיאה בביטול הרשמה")
                );
    }

    public void resetCancelSuccess() {
        cancelSuccess.postValue(false);
    }
}
