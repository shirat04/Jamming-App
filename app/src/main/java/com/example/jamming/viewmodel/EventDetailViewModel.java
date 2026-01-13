package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;

public class EventDetailViewModel extends ViewModel {

    private final EventRepository eventRepository = new EventRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAlreadyRegistered = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showAlreadyRegisteredMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getCancelSuccess() {
        return cancelSuccess;
    }

    public LiveData<Boolean> getShowAlreadyRegisteredMessage() {
        return showAlreadyRegisteredMessage;
    }

    public LiveData<Boolean> getIsAlreadyRegistered() {
        return isAlreadyRegistered;
    }

    private String eventId;

    public LiveData<Event> getEventLiveData() {
        return eventLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public void loadEvent(String eventId) {
        if (this.eventId != null && this.eventId.equals(eventId)) {
            return;
        }
        this.eventId = eventId;

        eventRepository.getEventById(eventId)
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        errorMessage.postValue("שגיאה: אירוע לא נמצא.");
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event != null) {
                        eventLiveData.postValue(event);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("שגיאה: אירוע לא נמצא.");
                });

        String uid = authRepository.getCurrentUid();
        if (uid == null) return;


        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(events ->
                        isAlreadyRegistered.postValue(events.contains(eventId))
                );
    }

    public void registerToEvent() {

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            errorMessage.postValue("שגיאה: משתמש לא מחובר");
            return;
        }

        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(registeredEvents -> {

                    if (registeredEvents.contains(eventId)) {
                        isAlreadyRegistered.postValue(true);
                        showAlreadyRegisteredMessage.postValue(true);
                        return;
                    }

                    eventRepository.registerUserIfCapacityAvailable(
                            eventId,
                            uid,
                            () -> {
                                registrationSuccess.postValue(true);
                                isAlreadyRegistered.postValue(true);
                                eventRepository.getEventById(eventId)
                                        .addOnSuccessListener(doc -> {
                                            Event updated = doc.toObject(Event.class);
                                            if (updated != null) {
                                                eventLiveData.postValue(updated);
                                            }
                                        });
                            },
                            msg -> errorMessage.postValue(msg)
                    );
                });
    }

    public void cancelRegistration() {

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            errorMessage.postValue("שגיאה: משתמש לא מחובר");
            return;
        }

        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(registeredEvents -> {

                    if (!registeredEvents.contains(eventId)) {
                        return;
                    }

                    userRepository.unregisterEventForUser(uid, eventId)
                            .addOnSuccessListener(unused -> {

                                eventRepository.decrementReserved(eventId)
                                        .addOnSuccessListener(u -> {

                                            isAlreadyRegistered.postValue(false);

                                            isAlreadyRegistered.postValue(false);
                                            cancelSuccess.postValue(true);

                                            eventRepository.getEventById(eventId)
                                                    .addOnSuccessListener(doc -> {
                                                        Event updated = doc.toObject(Event.class);
                                                        if (updated != null) {
                                                            eventLiveData.postValue(updated);
                                                        }
                                                    });


                                        })
                                        .addOnFailureListener(e ->
                                                errorMessage.postValue("שגיאה בעדכון האירוע")
                                        );
                            })
                            .addOnFailureListener(e ->
                                    errorMessage.postValue("שגיאה בביטול הרשמה")
                            );
                });
    }
    public void resetCancelSuccess() {
        cancelSuccess.postValue(false);
    }

    public void resetRegistrationSuccess() {
        registrationSuccess.postValue(false);
    }

    public void resetAlreadyRegisteredMessage() {
        showAlreadyRegisteredMessage.postValue(false);
    }

}
