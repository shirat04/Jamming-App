package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;

/**
 * ViewModel for the Event Details screen.
 *
 * Responsible for:
 * - Loading event data
 * - Handling registration and cancellation logic
 * - Exposing UI state via LiveData according to MVVM architecture
 */
public class EventDetailViewModel extends ViewModel {

    // Repositories used for data access and business logic
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    /**
     * Default constructor used in production.
     * Initializes repositories with their default implementations.
     */
    public EventDetailViewModel() {
        this(new EventRepository(), new UserRepository(), new AuthRepository());
    }

    /**
     * Constructor used mainly for testing (dependency injection).
     */
    public EventDetailViewModel(
            EventRepository eventRepository,
            UserRepository userRepository,
            AuthRepository authRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    /**
     * One-time UI events used for showing messages such as Toasts.
     */
    public enum UiEvent {
        REGISTER_SUCCESS,
        CANCEL_SUCCESS,
        ALREADY_REGISTERED
    }

    // Holds the current event data
    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();

    // Holds the current registration-related UI state
    private final MutableLiveData<RegistrationUiState> registrationUiState = new MutableLiveData<>();

    // Holds error messages to be displayed in the UI
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Indicates whether data is currently loading
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    // One-time UI events (success / already registered)
    private final MutableLiveData<UiEvent> uiEvent = new MutableLiveData<>();
    public LiveData<UiEvent> getUiEvent() { return uiEvent; }
    public LiveData<Boolean> getIsLoading() {return isLoading;}
    public LiveData<Event> getEventLiveData() { return eventLiveData; }
    public LiveData<RegistrationUiState> getRegistrationUiState() { return registrationUiState; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Currently loaded event ID (used to avoid unnecessary reloads)
    private String eventId;


    /**
     * Loads event details and updates UI state accordingly.
     *
     * @param eventId ID of the event to load
     */
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

                    // Check if the user is already registered to this event
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


    /**
     * Attempts to register the current user to the event.
     */
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

        // Check if user is already registered
        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(events -> {

                    if (events.contains(eventId)) {
                        uiEvent.postValue(UiEvent.ALREADY_REGISTERED);
                        updateRegistrationState(event, true);
                        return;
                    }

                    // Try to register user if capacity allows
                    eventRepository.registerUserIfCapacityAvailable(eventId, uid)
                            .addOnSuccessListener(v -> {
                                uiEvent.postValue(UiEvent.REGISTER_SUCCESS);

                                // Reload event to update capacity and UI
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


    /**
     * Cancels the current user's registration to the event.
     */
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

                    // Decrease reserved count after successful cancellation
                    eventRepository.decrementReserved(eventId)
                            .addOnSuccessListener(v -> {
                                uiEvent.postValue(UiEvent.CANCEL_SUCCESS);

                                // Reload event data
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

    /**
     * Updates the registration-related UI state based on event status and user registration.
     */
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

    /**
     * Checks whether the event date has already passed.
     */
    private boolean isEventExpired(Event event) {
        return event.getDateTime() < System.currentTimeMillis();
    }

    /**
     * Represents the UI state related to event registration.
     */
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