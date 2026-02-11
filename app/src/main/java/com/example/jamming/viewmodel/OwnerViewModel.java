package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel responsible for managing the owner's main screen.
 *
 * Handles loading the owner's name, fetching and categorizing events
 * (upcoming vs past), and deleting events.
 *
 * Follows the MVVM pattern:
 * - No direct UI references
 * - Exposes state via LiveData
 * - Delegates data access to repositories
 */
public class OwnerViewModel extends ViewModel {

    /** Repository for authentication-related operations */
    private final AuthRepository authRepo;

    /** Repository for event-related data */
    private final EventRepository eventRepo;

    /** Repository for user-related data */
    private final UserRepository userRepo;

    /**
     * Default constructor used in production.
     * Initializes the ViewModel with real repository instances.
     */
    public OwnerViewModel() {
        this(new AuthRepository(), new EventRepository(), new UserRepository());
    }

    /**
     * Constructor for dependency injection (mainly for testing).
     *
     * @param authRepo  authentication repository
     * @param eventRepo event repository
     * @param userRepo  user repository
     */
    public OwnerViewModel(AuthRepository authRepo, EventRepository eventRepo, UserRepository userRepo) {
        this.authRepo = authRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    /** Owner's display name */
    private final MutableLiveData<String> ownerName = new MutableLiveData<>();

    /** General message for UI feedback (errors, confirmations) */
    private final MutableLiveData<Integer> message = new MutableLiveData<>();

    /** List of upcoming events created by the owner */
    private final MutableLiveData<List<Event>> upcomingEvents = new MutableLiveData<>();

    /** List of past events created by the owner */
    private final MutableLiveData<List<Event>> pastEvents = new MutableLiveData<>();
    private static final Set<String> notifiedEvents = new HashSet<>();

    /** Read-only accessors for the View */
    public LiveData<String> getOwnerName() { return ownerName; }
    public LiveData<Integer> getMessage() { return message; }
    public LiveData<List<Event>> getUpcomingEvents() { return upcomingEvents; }
    public LiveData<List<Event>> getPastEvents() { return pastEvents; }


    /**
     * Loads the owner's display name.
     *
     * If the user is not logged in or the name cannot be retrieved,
     * a default value ("Owner") is used.
     */
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

    /**
     * Loads all events created by the current owner.
     *
     * Events are split into:
     * - Upcoming events (future date)
     * - Past events (already occurred)
     *
     * Each list is sorted by date for display purposes.
     */
    public void loadOwnerEvents() {
        String uid = authRepo.getCurrentUid();

        if (uid == null) {
            message.setValue(R.string.error_user_not_logged_in);
            return;
        }

        eventRepo.getOwnerEventsMapped(uid)
                .addOnSuccessListener(events  -> {

                    List<Event> upcoming = new ArrayList<>();
                    List<Event> past = new ArrayList<>();

                    long now = System.currentTimeMillis();

                    for (Event event : events) {
                        if (event.getDateTime() < now) {
                            past.add(event);
                        } else {
                            upcoming.add(event);
                        }
                    }

                    // Sort upcoming events from nearest to farthest
                    upcoming.sort((a, b) ->
                            Long.compare(a.getDateTime(), b.getDateTime()));

                    // Sort past events from most recent to oldest
                    past.sort((a, b) ->
                            Long.compare(b.getDateTime(), a.getDateTime()));

                    upcomingEvents.setValue(upcoming);
                    pastEvents.setValue(past);
                })
                .addOnFailureListener(e -> {
                    message.setValue(R.string.error_failed_to_load_events);
                });
    }

    /**
     * Deletes an event owned by the current user.
     *
     * After successful deletion, the event list is reloaded.
     *
     * @param eventId Event ID to delete
     */
    public void deleteEvent(String eventId) {
        eventRepo.deleteEvent(eventId)
                .addOnSuccessListener(v -> {
                    message.setValue(R.string.event_deleted_success);
                    loadOwnerEvents();
                })
                .addOnFailureListener(e ->
                        message.setValue(R.string.error_deleting_event)
                );
    }





    public void startCapacityMonitoring(String ownerId, EventRepository.OnEventFullListener listener) {
        eventRepo.startMonitoringAllMyEvents(ownerId, (eventId, eventName) -> {


            if (!notifiedEvents.contains(eventId)) {


                notifiedEvents.add(eventId);

                if (listener != null) {
                    listener.onEventFull(eventId, eventName);
                }
            } else {

                android.util.Log.d("DEBUG", "Blocked duplicate notification for: " + eventName);
            }
        });
    }



}
