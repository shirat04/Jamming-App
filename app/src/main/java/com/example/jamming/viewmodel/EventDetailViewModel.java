package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;


public class EventDetailViewModel extends ViewModel {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public EventDetailViewModel() {
        this(new EventRepository(), new UserRepository(), new AuthRepository());
    }

    public EventDetailViewModel(
            EventRepository eventRepository,
            UserRepository userRepository,
            AuthRepository authRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<RegistrationUiState> registrationUiState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showAlreadyRegisteredMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public LiveData<Boolean> getIsLoading() {return isLoading;}

    public LiveData<Event> getEventLiveData() { return eventLiveData; }
    public LiveData<RegistrationUiState> getRegistrationUiState() { return registrationUiState; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }
    public LiveData<Boolean> getCancelSuccess() { return cancelSuccess; }
    public LiveData<Boolean> getShowAlreadyRegisteredMessage() { return showAlreadyRegisteredMessage; }

    private String eventId;


    public void loadEvent(String eventId) {
        if (eventId == null || eventId.equals(this.eventId)) {
            return;
        }
        isLoading.postValue(true);
        this.eventId = eventId;


        eventRepository.getEventById(eventId)
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        errorMessage.postValue("Event not found");
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event == null) {
                        errorMessage.postValue("Event data is invalid");
                        return;
                    }

                    eventLiveData.postValue(event);

                    String uid = authRepository.getCurrentUid();
                    if (uid == null) {
                        errorMessage.postValue("User not logged in");
                        registrationUiState.postValue(null);
                        return;
                    }

                    userRepository.getRegisteredEvents(uid)
                            .addOnSuccessListener(events -> {
                                boolean registered = events.contains(eventId);
                                updateRegistrationState(event, registered);
                                isLoading.postValue(false);
                            })
                            .addOnFailureListener(e -> {
                                updateRegistrationState(event, false);
                                isLoading.postValue(false);
                            });

                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to load event");
                    isLoading.postValue(false);
                });


    }

    public void registerToEvent() {
        Event event = eventLiveData.getValue();
        if (event == null) {
            errorMessage.postValue("Event not found");
            return;
        }

        if (isEventExpired(event)) {
            errorMessage.postValue("This event has already ended");
            return;
        }

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            errorMessage.postValue("User not logged in");
            return;
        }

        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(events -> {

                    if (events.contains(eventId)) {
                        showAlreadyRegisteredMessage.postValue(true);
                        updateRegistrationState(event, true);
                        return;
                    }

                    eventRepository.registerUserIfCapacityAvailable(eventId, uid)
                            .addOnSuccessListener(v -> {
                                registrationSuccess.postValue(true);

                                eventRepository.getEventById(eventId)
                                        .addOnSuccessListener(doc -> {
                                            Event refreshed = doc.toObject(Event.class);
                                            if (refreshed != null) {
                                                eventLiveData.postValue(refreshed);
                                                updateRegistrationState(refreshed, true);
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if ("EVENT_FULL".equals(e.getMessage())) {
                                    errorMessage.postValue("The event is full");
                                } else {
                                    errorMessage.postValue("Registration failed");
                                }
                            });

                });


    }

    public void cancelRegistration() {
        Event event = eventLiveData.getValue();
        if (event == null) return;

        if (isEventExpired(event)) {
            errorMessage.postValue("This event has already ended");
            return;
        }

        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            errorMessage.postValue("User not logged in");
            return;
        }

        userRepository.unregisterEventForUser(uid, eventId)
                .addOnSuccessListener(unused -> {

                    eventRepository.decrementReserved(eventId)
                            .addOnSuccessListener(v -> {

                                eventRepository.getEventById(eventId)
                                        .addOnSuccessListener(doc -> {
                                            Event refreshed = doc.toObject(Event.class);
                                            if (refreshed != null) {
                                                eventLiveData.postValue(refreshed);
                                                updateRegistrationState(refreshed, false);
                                            }
                                        });

                            })
                            .addOnFailureListener(e ->
                                    errorMessage.postValue("Failed to update event capacity")
                            );
                })
                .addOnFailureListener(e ->
                        errorMessage.postValue("Failed to cancel registration")
                );
    }

    private void updateRegistrationState(Event event, boolean isRegistered) {
        boolean isPast = isEventExpired(event);
        boolean isFull = event.getReserved() >= event.getMaxCapacity();

        if (isPast) {
            registrationUiState.postValue(
                    new RegistrationUiState(
                            false,
                            false,
                            "The event has ended",
                            null
                    )
            );
            return;
        }

        if (isRegistered) {
            registrationUiState.postValue(
                    new RegistrationUiState(
                            false,
                            true,
                            "You're registered",
                            isFull ? "SOLD OUT" : null
                    )
            );
            return;
        }

        if (isFull) {
            registrationUiState.postValue(
                    new RegistrationUiState(
                            false,
                            false,
                            "SOLD OUT",
                            null
                    )
            );
            return;
        }

        registrationUiState.postValue(
                new RegistrationUiState(
                        true,
                        false,
                        "Register for event",
                        null
                )
        );
    }

    private boolean isEventExpired(Event event) {
        return event.getDateTime() < System.currentTimeMillis();
    }

    public void resetRegistrationSuccess() {
        registrationSuccess.postValue(false);
    }

    public void resetCancelSuccess() {
        cancelSuccess.postValue(false);
    }

    public void resetAlreadyRegisteredMessage() {
        showAlreadyRegisteredMessage.postValue(false);
    }

    public static class RegistrationUiState {
        public final boolean canRegister;
        public final boolean canCancel;
        public final String mainText;     // Registered / Register / Ended
        public final String secondaryText; // SOLD OUT / null

        public RegistrationUiState(
                boolean canRegister,
                boolean canCancel,
                String mainText,
                String secondaryText
        ) {
            this.canRegister = canRegister;
            this.canCancel = canCancel;
            this.mainText = mainText;
            this.secondaryText = secondaryText;
        }
    }



}
