package com.example.jamming.viewmodel;

import android.util.Log;

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

                    userRepository.registerEventForUser(uid, eventId)
                            .addOnSuccessListener(unused -> {

                                eventRepository.incrementReserved(eventId)
                                        .addOnSuccessListener(u -> {

                                            Event current = eventLiveData.getValue();
                                            if (current != null) {
                                                current.setReserved(current.getReserved() + 1);
                                                eventLiveData.postValue(current);
                                            }

                                            registrationSuccess.postValue(true);
                                            isAlreadyRegistered.postValue(true);
                                        })
                                        .addOnFailureListener(e ->
                                                errorMessage.postValue("שגיאה בעדכון האירוע")
                                        );
                            })
                            .addOnFailureListener(e ->
                                    errorMessage.postValue("שגיאה בהרשמת משתמש")
                            );
                })
                .addOnFailureListener(e ->
                        errorMessage.postValue("שגיאה בטעינת נתוני משתמש")
                );
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

                                            Event current = eventLiveData.getValue();
                                            if (current != null) {
                                                current.setReserved(
                                                        Math.max(0, current.getReserved() - 1)
                                                );
                                                eventLiveData.postValue(current);
                                            }
                                            cancelSuccess.postValue(true);

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
