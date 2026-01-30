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

    private final MutableLiveData<RegistrationUiState> registrationUiState = new MutableLiveData<>();
    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showAlreadyRegisteredMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getCancelSuccess() {
        return cancelSuccess;
    }
    public LiveData<Boolean> getShowAlreadyRegisteredMessage() {return showAlreadyRegisteredMessage;}
    public LiveData<Event> getEventLiveData() {
        return eventLiveData;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<RegistrationUiState> getRegistrationUiState() {return registrationUiState;}
    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }
    private String eventId;

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
                    if (event == null) return;

                    eventLiveData.postValue(event);

                    String uid = authRepository.getCurrentUid();
                    if (uid == null) {
                        errorMessage.postValue("שגיאה: משתמש לא מחובר");
                        return;
                    }

                    userRepository.getRegisteredEvents(uid)
                            .addOnSuccessListener(events -> {
                                boolean registered = events.contains(eventId);
                                updateRegistrationState(event, registered);
                            })
                            .addOnFailureListener(e -> {
                                updateRegistrationState(event, false);
                            });

                })
                .addOnFailureListener(e ->
                        errorMessage.postValue("שגיאה: אירוע לא נמצא.")
                );
    }

    public void registerToEvent() {
        Event event = eventLiveData.getValue();
        if (event != null && event.getDateTime() < System.currentTimeMillis()) {
            errorMessage.postValue("This event has already ended");
            return;
        }

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            errorMessage.postValue("שגיאה: משתמש לא מחובר");
            return;
        }

        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(registeredEvents -> {

                    if (registeredEvents.contains(eventId)) {
                        showAlreadyRegisteredMessage.postValue(true);
                        Event currentEvent = eventLiveData.getValue();
                        if (currentEvent != null) {
                            updateRegistrationState(currentEvent, true);
                        }
                        return;
                    }

                    eventRepository.registerUserIfCapacityAvailable(
                            eventId,
                            uid,
                            () -> {
                                registrationSuccess.postValue(true);

                                eventRepository.getEventById(eventId)
                                        .addOnSuccessListener(doc -> {
                                            Event updated = doc.toObject(Event.class);
                                            if (updated != null) {
                                                eventLiveData.postValue(updated);
                                                updateRegistrationState(updated, true);
                                            }
                                        });
                            },
                            msg -> errorMessage.postValue(msg)
                    );
                });
    }

    public void cancelRegistration() {
        Event event = eventLiveData.getValue();
        if (event != null && event.getDateTime() < System.currentTimeMillis()) {
            errorMessage.postValue("This event has already ended");
            return;
        }

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

                                            cancelSuccess.postValue(true);

                                            eventRepository.getEventById(eventId)
                                                    .addOnSuccessListener(doc -> {
                                                        Event updated = doc.toObject(Event.class);
                                                        if (updated != null) {
                                                            eventLiveData.postValue(updated);
                                                            updateRegistrationState(updated, false);
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

    public static class RegistrationUiState {
        public final boolean canRegister;
        public final boolean canCancel;
        public final String registerButtonText;

        public RegistrationUiState(
                boolean canRegister,
                boolean canCancel,
                String registerButtonText
        ) {
            this.canRegister = canRegister;
            this.canCancel = canCancel;
            this.registerButtonText = registerButtonText;
        }
    }

    private void updateRegistrationState(Event event, boolean isRegistered) {
        boolean isFull = event.getReserved() >= event.getMaxCapacity();
        boolean isPast = event.getDateTime() < System.currentTimeMillis();

        if (isPast) {
            registrationUiState.postValue(
                    new RegistrationUiState(false, false, "The event has ended")
            );
            return;
        }
        if (isFull) {
            registrationUiState.postValue(new RegistrationUiState(false, isRegistered, "SOLD OUT"));
            return;
        }
        if (isRegistered) {
            registrationUiState.postValue(
                    new RegistrationUiState(false, true, "You're registered"));
            return;
        }
        registrationUiState.postValue(
                new RegistrationUiState(true, false, "Register for event"));
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
