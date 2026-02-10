package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.Event;
import com.example.jamming.repository.EventRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class EventDetailViewModel extends ViewModel {

    private final EventRepository repository;
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // 1. המשתנה החדש שמחזיק את סטטוס ההרשמה
    private final MutableLiveData<Boolean> isRegistered = new MutableLiveData<>(false);

    private final String currentUserId;

    public EventDetailViewModel() {
        repository = new EventRepository();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = null;
        }
    }

    // טעינת אירוע ובדיקה האם המשתמש כבר רשום אליו
    public void loadEvent(String eventId) {
        repository.getEventById(eventId).addOnSuccessListener(documentSnapshot -> {
            Event loadedEvent = documentSnapshot.toObject(Event.class);
            if (loadedEvent != null) {
                loadedEvent.setId(documentSnapshot.getId());
                event.setValue(loadedEvent);

                // 2. בדיקה: האם המשתמש שלי נמצא ברשימת המשתתפים?
                checkIfRegistered(loadedEvent);
            }
        }).addOnFailureListener(e -> {
            error.setValue(e.getMessage());
        });
    }

    // פונקציית עזר לבדיקת הרשמה
    private void checkIfRegistered(Event event) {
        if (currentUserId == null) return;

        List<String> participants = event.getParticipants();
        if (participants != null && participants.contains(currentUserId)) {
            isRegistered.setValue(true);
        } else {
            isRegistered.setValue(false);
        }
    }

    public void registerToEvent() {
        Event currentEvent = event.getValue();
        if (currentEvent == null || currentUserId == null) return;

        repository.registerUserIfCapacityAvailable(currentEvent.getId(), currentUserId)
                .addOnSuccessListener(aVoid -> {
                    // עדכון מוצלח
                    isRegistered.setValue(true);
                    // רענון האירוע כדי לראות את מספרי המקומות מתעדכנים
                    loadEvent(currentEvent.getId());
                })
                .addOnFailureListener(e -> {
                    error.setValue("Registration failed: " + e.getMessage());
                });
    }

    // 3. הפונקציה שהייתה חסרה לך!
    public LiveData<Boolean> getRegistrationStatus() {
        return isRegistered;
    }

    public LiveData<Event> getEvent() {
        return event;
    }

    public LiveData<String> getError() {
        return error;
    }
}