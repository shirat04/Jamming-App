package com.example.jamming.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import com.example.jamming.utils.GeoUtils;

import java.util.ArrayList;
import java.util.List;

public class ExploreEventsViewModel extends ViewModel {
    private boolean isNearMeMode = false;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AuthRepository authRepository;


    private final MutableLiveData<String> userGreeting = new MutableLiveData<>();
    private final MutableLiveData<List<Event>> events = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>();
    private Location lastKnownLocation;

    private static final int DEFAULT_RADIUS_KM = 10;

    public ExploreEventsViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        eventRepository = new EventRepository();
    }

    public LiveData<String> getUserGreeting() {
        return userGreeting;
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public LiveData<Boolean> getIsEmpty() {
        return isEmpty;
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

    public void onUserLocationAvailable(Location location) {
        this.lastKnownLocation = location;
        if (isNearMeMode) {
            loadEventsNearMe();
        }
    }
    public void loadAllEvents() {
        isNearMeMode = false;
        eventRepository.getActiveEvents()
                .addOnSuccessListener(allEvents -> {
                    events.setValue(allEvents);
                    isEmpty.setValue(allEvents.isEmpty());
                })
                .addOnFailureListener(e -> {
                    events.setValue(new ArrayList<>());
                    isEmpty.setValue(true);
                });
    }

    public void loadEventsNearMe() {
        isNearMeMode = true;
        if (lastKnownLocation == null) {
            isEmpty.setValue(true);
            return;
        }

        eventRepository.getActiveEvents()
                .addOnSuccessListener(allEvents -> {

                    List<Event> filtered = new ArrayList<>();

                    for (Event event : allEvents) {
                        if (event.getLatitude() == 0 || event.getLongitude() == 0)
                            continue;

                        double distanceKm = GeoUtils.distanceInKm(
                                lastKnownLocation,
                                event.getLatitude(),
                                event.getLongitude()
                        );

                        if (distanceKm <= DEFAULT_RADIUS_KM) {
                            filtered.add(event);
                        }
                    }

                    events.setValue(filtered);
                    isEmpty.setValue(filtered.isEmpty());
                })
                .addOnFailureListener(e -> {
                    events.setValue(new ArrayList<>());
                    isEmpty.setValue(true);
                });
    }
}

