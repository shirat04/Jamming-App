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

public class ExploreEventsViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AuthRepository authRepository;
    public enum EmptyState {
        NONE,
        NO_EVENTS_AT_ALL,
        NO_MATCHING_FILTERS
    }
    public ExploreEventsViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        eventRepository = new EventRepository();
    }
    private final MutableLiveData<String> userGreeting = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<EmptyState> emptyState = new MutableLiveData<>(EmptyState.NONE);

    public LiveData<EmptyState> getEmptyState() {return emptyState;}
    private final MutableLiveData<EventFilter> filter = new MutableLiveData<>();

    private final MutableLiveData<List<Event>> filteredEvents = new MutableLiveData<>(new ArrayList<>());

    private List<Event> allEvents = new ArrayList<>();
    public LiveData<String> getUserGreeting() {
        return userGreeting;
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }
    public LiveData<EventFilter> getFilter() {return filter;}
    public LiveData<List<Event>> getFilteredEvents() {return filteredEvents;}

    public void updateFilter(FilterUpdater updater) {
        EventFilter current = filter.getValue();
        if (current == null) {
            current = new EventFilter();
        }

        updater.update(current);
        filter.setValue(current);
        applyFilter();

        String uid = authRepository.getCurrentUid();
        if (uid != null) {
            userRepository.saveLastEventFilter(uid, current);
        }
    }

    private void updateEmptyState(
            List<Event> allEvents,
            List<Event> filteredEvents
    ) {
        if (allEvents == null || allEvents.isEmpty()) {
            emptyState.postValue(EmptyState.NO_EVENTS_AT_ALL);
        } else if (filteredEvents == null || filteredEvents.isEmpty()) {
            emptyState.postValue(EmptyState.NO_MATCHING_FILTERS);
        } else {
            emptyState.postValue(EmptyState.NONE);
        }
    }


    public void initFilter() {
        String uid = authRepository.getCurrentUid();
        if (uid == null) {
            filter.setValue(new EventFilter());
            return;
        }

        userRepository.getLastEventFilter(uid)
                .addOnSuccessListener(eventFilter -> {
                    if (eventFilter != null) {
                        filter.setValue(eventFilter);
                    } else {
                        filter.setValue(new EventFilter());
                    }
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    filter.setValue(new EventFilter());
                    applyFilter();
                });
    }


    public void loadUserGreeting() {
        String uid = authRepository.getCurrentUid();
        if (uid == null) return;

        userRepository.getUserById(uid)
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("fullName");
                    if (name == null) name = "";
                    userGreeting.setValue("Hello " + name);
                });
    }


    public void loadAllEvents() {
        eventRepository.getActiveEvents()
                .addOnSuccessListener(fetchedEvents -> {
                    this.allEvents = fetchedEvents;
                    events.setValue(allEvents);

                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    this.allEvents = new ArrayList<>();
                    events.setValue(new ArrayList<>());
                    filteredEvents.setValue(new ArrayList<>());

                    updateEmptyState(allEvents, filteredEvents.getValue());
                });
    }


    private void applyFilter() {
        EventFilter f = filter.getValue();
        List<Event> result = EventFilterEngine.filter(allEvents, f);
        filteredEvents.setValue(result);
        updateEmptyState(allEvents, result);
    }

    public void clearFilter() {
        EventFilter reset = new EventFilter();
        filter.setValue(reset);
        applyFilter();

        String uid = authRepository.getCurrentUid();
        if (uid != null) {
            userRepository.saveLastEventFilter(uid, reset);
        }
    }

    public interface FilterUpdater {
        void update(EventFilter filter);
    }
}

