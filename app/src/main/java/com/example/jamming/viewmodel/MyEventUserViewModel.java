package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel responsible for managing the "My Events" screen.
 *
 * This ViewModel:
 * - Loads events the user is registered for
 * - Handles unregistering from events
 * - Exposes UI state via LiveData according to MVVM principles
 *
 * It contains no UI logic and does not reference any View or Context.
 */
public class MyEventUserViewModel extends ViewModel {

    // Repository for user-related data (registered events, user actions)
    private final UserRepository userRepository;

    // Repository for event-related data (event details, capacity updates)
    private final EventRepository eventRepository;

    // Repository for authentication and user identity
    private final AuthRepository authRepository;

    /**
     * Default constructor used in production.
     * Initializes repositories with their default implementations.
     */
    public MyEventUserViewModel() {
        this(new UserRepository(), new EventRepository(), new AuthRepository());
    }

    /**
     * Constructor for dependency injection (mainly used for testing).
     */
    public MyEventUserViewModel(
            UserRepository userRepository,
            EventRepository eventRepository,
            AuthRepository authRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.authRepository = authRepository;
    }

    // List of events the user is registered for (wrapped with event ID)
    private final MutableLiveData<List<EventWithId>> myEvents = new MutableLiveData<>();

    // Indicates whether a cancel/remove action completed successfully
    private final MutableLiveData<Boolean> cancelSuccess = new MutableLiveData<>();

    // Represents the overall state of the "My Events" screen
    private final MutableLiveData<MyEventsState> state = new MutableLiveData<>();

    /* ===== LiveData getters ===== */
    public LiveData<MyEventsState> getState() { return state; }
    public LiveData<List<EventWithId>> getMyEvents() {
        return myEvents;
    }
    public LiveData<Boolean> getCancelSuccess() {
        return cancelSuccess;
    }

    /**
     * Represents high-level UI states for the "My Events" screen.
     * The Activity decides how to present each state.
     */
    public enum MyEventsState {
        NONE,                   // Normal state, events loaded successfully
        NOT_LOGGED_IN,           // User is not authenticated
        NO_REGISTERED_EVENTS,    // User has no registered events
        LOAD_ERROR               // Failed to load events or perform an action
    }

    /**
     * Wrapper class that pairs an Event with its Firestore document ID.
     * This avoids mixing Firestore IDs into the Event model itself.
     */
    public static class EventWithId {
        public final String id;
        public final Event event;


        public EventWithId(String id, Event event) {
            this.id = id;
            this.event = event;
        }
    }

    /**
     * Loads all events the currently logged-in user is registered for.
     * Updates screen state according to the result.
     */
    public void loadMyEvents() {
        String uid = authRepository.getCurrentUid();
        // User is not logged in
        if (uid == null) {
            state.setValue(MyEventsState.NOT_LOGGED_IN);
            myEvents.setValue(new ArrayList<>());
            return;
        }

        // User has no registered events
        userRepository.getRegisteredEvents(uid)
                .addOnSuccessListener(ids -> {
                    if (ids == null || ids.isEmpty()) {
                        state.setValue(MyEventsState.NO_REGISTERED_EVENTS);
                        myEvents.setValue(new ArrayList<>());
                        return;
                    }

                    // Load event documents by their IDs
                    loadEventsByIds(ids);
                })
                .addOnFailureListener(e ->
                        state.setValue(MyEventsState.LOAD_ERROR)
                );
    }

    /**
     * Cancels the user's registration for an upcoming event.
     *
     * @param eventId ID of the event to cancel registration for
     */
    public void cancelRegistration(String eventId) {
        unregister(eventId, true);
    }

    /**
     * Removes a past event from the user's list without
     * updating the event's reserved counter.
     *
     * @param eventId ID of the event to remove
     */
    public void removeEventFromMyList(String eventId) {
        unregister(eventId, false);
    }

    /**
     * Resets the cancel success flag after it was consumed by the UI.
     */
    public void resetCancelSuccess() {
        cancelSuccess.setValue(false);
    }

    /**
     * Loads event documents by their IDs in chunks of up to 10,
     * due to Firestore query limitations.
     *
     * @param ids list of event document IDs
     */
    private void loadEventsByIds(List<String> ids) {
        List<EventWithId> result = new ArrayList<>();

        // Firestore allows a maximum of 10 IDs in a whereIn query
        int totalChunks = (int) Math.ceil(ids.size() / 10.0);
        // Mutable counter to track how many chunks finished loading
        int[] completedChunks = {0};

        for (int i = 0; i < ids.size(); i += 10) {
            // Extract a sublist of up to 10 IDs
            List<String> chunk = ids.subList(i, Math.min(i + 10, ids.size()));

            eventRepository.getEventsByIds(chunk)
                    .addOnSuccessListener(snapshot -> {
                        for (var doc : snapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                result.add(new EventWithId(doc.getId(), event));
                            }
                        }

                        // Mark this chunk as completed
                        completedChunks[0]++;

                        if (completedChunks[0] == totalChunks) {
                            myEvents.setValue(sortEventsLogically(result));
                            state.setValue(MyEventsState.NONE);
                        }
                    })
                    .addOnFailureListener(e ->
                            state.setValue(MyEventsState.LOAD_ERROR)
                    );
        }
    }

    /**
     * Handles unregistering a user from an event.
     *
     * @param eventId        event ID
     * @param updateReserved whether to decrement the reserved counter
     */
    private void unregister(String eventId, boolean updateReserved) {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;

        userRepository.unregisterEventForUser(uid, eventId)
                .addOnSuccessListener(unused -> {
                    if (updateReserved) {
                        eventRepository.decrementReserved(eventId);
                    }
                    cancelSuccess.setValue(true);
                    loadMyEvents();
                })
                .addOnFailureListener(e ->
                        state.setValue(MyEventsState.LOAD_ERROR)
                );
    }

    /**
     * Sorts events so that:
     * - Future events appear first (ascending by date)
     * - Past events appear last (descending by date)
     *
     * @param events list of events to sort
     * @return logically sorted list of events
     */
    private List<EventWithId> sortEventsLogically(List<EventWithId> events) {
        long now = System.currentTimeMillis();

        List<EventWithId> future = new ArrayList<>();
        List<EventWithId> past = new ArrayList<>();

        for (EventWithId e : events) {
            if (e.event.getDateTime() >= now) {
                future.add(e);
            } else {
                past.add(e);
            }
        }
        future.sort(Comparator.comparingLong(e -> e.event.getDateTime()));

        past.sort((a, b) ->
                Long.compare(b.event.getDateTime(), a.event.getDateTime())
        );

        List<EventWithId> result = new ArrayList<>();
        result.addAll(future);
        result.addAll(past);

        return result;
    }

}
