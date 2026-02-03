package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.model.EventFilter;
import com.example.jamming.model.EventFilterEngine;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;


/**
 * ViewModel responsible for managing the state and business logic
 * of the "Explore Events" screen.
 *
 * This class follows the MVVM architecture pattern:
 * - Holds screen-related state using LiveData
 * - Communicates with repositories to retrieve and persist data
 * - Applies filtering logic without direct interaction with the UI
 *
 * The ViewModel is lifecycle-aware and survives configuration changes.
 */
public class ExploreEventsViewModel extends ViewModel {

    /** Repository for accessing user-related data */
    private final UserRepository userRepository;

    /** Repository for accessing event-related data */
    private final EventRepository eventRepository;

    /** Repository for authentication and user identity */
    private final AuthRepository authRepository;

    /**
     * Default constructor used in production.
     * Initializes ViewModel with concrete repository implementations.
     */
    public ExploreEventsViewModel(){
        this(new AuthRepository(), new UserRepository(), new EventRepository());
    }


    /**
     * Constructor for dependency injection.
     * Allows providing mock or fake repositories for testing purposes.
     *
     * @param authRepository   authentication repository
     * @param userRepository   user data repository
     * @param eventRepository event data repository
     */
    public ExploreEventsViewModel(
            AuthRepository authRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }


    /**
     * Represents the logical empty state of the screen.
     * This state is consumed by the UI to display appropriate messages.
     */
    public enum EmptyState {

        /** There are events and at least one matches the current filter */
        NONE,

        /** No events exist in the system at all */
        NO_EVENTS_AT_ALL,

        /** Events exist, but none match the active filter */
        NO_MATCHING_FILTERS
    }


    /** Holds the full name of the currently logged-in user */
    private final MutableLiveData<String> userName = new MutableLiveData<>();

    /** Holds the current empty state of the screen */
    private final MutableLiveData<EmptyState> emptyState = new MutableLiveData<>(EmptyState.NONE);


    /** Holds the currently active event filter */
    private final MutableLiveData<EventFilter> filter = new MutableLiveData<>();

    /**
     * Holds the list of events that match the active filter.
     * This is the list observed and rendered by the UI.
     */
    private final MutableLiveData<List<Event>> filteredEvents = new MutableLiveData<>(new ArrayList<>());

    /**
     * Internal list of all fetched events.
     * Not exposed to the UI directly.
     */
    private List<Event> allEvents = new ArrayList<>();

    /* ===== LiveData getters (read-only exposure) ===== */
    public LiveData<EmptyState> getEmptyState() {return emptyState;}
    public LiveData<String> getUserName() {
        return userName;
    }
    public LiveData<EventFilter> getFilter() {return filter;}
    public LiveData<List<Event>> getFilteredEvents() {return filteredEvents;}

    /**
     * Loads the full name of the currently authenticated user.
     * The ViewModel exposes only the raw data, while the UI
     * decides how to present it (e.g., greeting text).
     */
    public void loadUserName() {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;

        userRepository.getUserById(uid)
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("fullName");
                    userName.setValue(name);
                });
    }

    /**
     * Loads all active events from the repository.
     * Once loaded, the current filter is applied automatically.
     */
    public void loadAllEvents() {
        eventRepository.getActiveEvents()
                .addOnSuccessListener(fetchedEvents -> {
                    allEvents = fetchedEvents;
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    allEvents = new ArrayList<>();
                    filteredEvents.setValue(new ArrayList<>());
                    updateEmptyState();
                });
    }

    /**
     * Clears all active filters and restores default filtering behavior.
     * The reset filter is persisted for the current user.
     */
    public void clearFilter() {
        setFilter(new EventFilter(), true);
    }

    /**
     * Updates the current filter using a functional updater.
     * This allows partial updates without exposing the filter's internal structure.
     *
     * @param updater a function that modifies the existing filter
     */
    public void updateFilter(FilterUpdater updater) {
        EventFilter current = filter.getValue();
        if (current == null) current = new EventFilter();

        updater.update(current);
        setFilter(current, true);
    }

    /**
     * Initializes the filter when the screen is first opened.
     * Attempts to restore the last saved filter of the user.
     */
    public void initFilter() {
        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            setFilter(new EventFilter(), false);
            return;
        }
        userRepository.getLastEventFilter(uid)
                .addOnSuccessListener(f ->
                        setFilter(f != null ? f : new EventFilter(), false))
                .addOnFailureListener(e ->
                        setFilter(new EventFilter(), false));
    }

    /**
     * Sets a new filter, applies it to the event list,
     * and optionally persists it for the current user.
     *
     * @param newFilter the filter to apply
     * @param persist   whether the filter should be saved for future sessions
     */
    private void setFilter(EventFilter newFilter, boolean persist) {
        filter.setValue(newFilter);
        applyFilter();

        if (persist) {
            String uid = authRepository.getCurrentUid();
            if (uid != null) {
                userRepository.saveLastEventFilter(uid, newFilter);
            }
        }
    }

    /**
     * Applies the current filter to the full event list
     * and updates the filtered events LiveData.
     */
    private void applyFilter() {
        EventFilter f = filter.getValue();
        List<Event> result = EventFilterEngine.filter(allEvents, f);
        filteredEvents.setValue(result);
        updateEmptyState();
    }

    /**
     * Updates the empty state based on the current data:
     * - No events in the system
     * - Events exist but none match the filter
     * - Normal state with visible events
     */
    private void updateEmptyState() {
        if (allEvents.isEmpty()) {
            emptyState.setValue(EmptyState.NO_EVENTS_AT_ALL);
        } else if (filteredEvents.getValue() == null ||
                filteredEvents.getValue().isEmpty()) {
            emptyState.setValue(EmptyState.NO_MATCHING_FILTERS);
        } else {
            emptyState.setValue(EmptyState.NONE);
        }
    }

    /**
     * Functional interface used to update parts of the EventFilter
     * without exposing its full structure to the View layer.
     */
    public interface FilterUpdater {
        void update(EventFilter filter);
    }
}

