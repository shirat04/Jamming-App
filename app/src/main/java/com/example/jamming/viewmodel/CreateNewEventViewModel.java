package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.model.EventField;
import com.example.jamming.utils.GenreUtils;
import com.example.jamming.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ViewModel responsible for managing the state and logic
 * of the "Create New Event" screen.
 *
 * Handles user input validation, UI state exposure via LiveData,
 * and communication with repositories.
 */
public class CreateNewEventViewModel extends ViewModel {
    private AuthRepository authRepo;
    private EventRepository eventRepository;

    /**
     * Default constructor used in production.
     * Initializes repositories with their default implementations.
     */
    public CreateNewEventViewModel() {
        this(new AuthRepository(), new EventRepository());
    }

    /**
     * Constructor for dependency injection (used mainly for testing).
     *
     * @param authRepo Authentication repository
     * @param eventRepository Event data repository
     */
    public CreateNewEventViewModel(AuthRepository authRepo,
                                   EventRepository eventRepository) {
        this.authRepo = authRepo;
        this.eventRepository = eventRepository;
    }

    /** Selected date and time of the event */
    private final Calendar dateTime = Calendar.getInstance();

    /** Selected location data */
    private double lat, lng;
    private String address;

    /** Selected music genres */
    private final List<MusicGenre> genres = new ArrayList<>();

    /** UI state exposed to the View */
    private final MutableLiveData<String> dateText = new MutableLiveData<>();
    private final MutableLiveData<String> timeText = new MutableLiveData<>();
    private final MutableLiveData<String> locationText = new MutableLiveData<>();
    private final MutableLiveData<String> genresText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> messageResId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<EventField> errorField = new MutableLiveData<>();

    /* ===== LiveData getters ===== */
    public LiveData<EventField> getErrorField() { return errorField; }
    public LiveData<String> getDateText() { return dateText; }
    public LiveData<String> getTimeText() { return timeText; }
    public LiveData<String> getLocationText() { return locationText; }
    public LiveData<String> getGenresText() {return genresText;}
    public LiveData<Integer> getMessageResId() {return messageResId;}
    public LiveData<Boolean> getSuccess() { return success; }
    public Calendar getDateTime() {return dateTime;}


    /**
     * Sets the selected date and updates the UI text.
     */
    public void setDate(int year, int month, int day) {
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month);
        dateTime.set(Calendar.DAY_OF_MONTH, day);
        dateText.setValue(
                DateUtils.formatOnlyDate(dateTime.getTimeInMillis())
        );
    }

    /**
     * Sets the selected time and updates the UI text.
     */
    public void setTime(int hour, int minute) {
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        timeText.setValue(
                DateUtils.formatOnlyTime(dateTime.getTimeInMillis())
        );
    }

    /**
     * Adds or removes a music genre based on user selection.
     */
    public void toggleGenre(MusicGenre genre, boolean checked) {
        if (checked && !genres.contains(genre)) {
            genres.add(genre);
        } else if (!checked) {
            genres.remove(genre);
        }

        genresText.setValue(GenreUtils.genresToTextFromEnums(genres));

        // Clear genre error once at least one genre is selected
        if (!genres.isEmpty() && errorField.getValue() == EventField.GENRE) {
            errorField.setValue(null);
        }
    }

    /**
     * Returns a boolean array representing which genres are selected.
     * Used to restore UI state.
     */
    public boolean[] getCheckedGenres(MusicGenre[] allGenres) {
        boolean[] checked = new boolean[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            checked[i] = genres.contains(allGenres[i]);
        }
        return checked;
    }

    /**
     * Updates location data after user selects a location on the map.
     */
    public void onLocationSelected(double lat, double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        locationText.setValue(address);
    }

    /**
     * Validates user input and attempts to create a new event.
     * Reports validation errors using EventField for precise UI feedback.
     */
    public void publish(String name, String capacity, String description) {
        errorField.setValue(null);

        if (name == null || name.trim().isEmpty()) {
            errorField.setValue(EventField.TITLE);
            return;
        }

        if (address == null || address.trim().isEmpty()) {
            errorField.setValue(EventField.LOCATION);
            return;
        }

        if (dateText.getValue() == null) {
            errorField.setValue(EventField.DATE);
            return;
        }

        if (timeText.getValue() == null) {
            errorField.setValue(EventField.TIME);
            return;
        }

        long now = System.currentTimeMillis();
        long selectedTime = dateTime.getTimeInMillis();

        // Prevent creation of events in the past
        if (selectedTime <= now) {
            errorField.setValue(EventField.TIME);
            messageResId.setValue(R.string.error_event_time_already_passed_create);
            return;
        }

        if (genres.isEmpty()) {
            errorField.setValue(EventField.GENRE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capacity);
            if (cap <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            errorField.setValue(EventField.CAPACITY);
            return;
        }

        if (description == null || description.trim().isEmpty()) {
            errorField.setValue(EventField.DESCRIPTION);
            return;
        }


        String ownerId = authRepo.getCurrentUid();
        if (ownerId == null) {
            messageResId.setValue(R.string.error_user_generic);
            return;
        }

        Event event = new Event(
                ownerId,
                name,
                description,
                GenreUtils.genresToStrings(genres),
                address,
                dateTime.getTimeInMillis(),
                cap,
                lat,
                lng
        );

        eventRepository.createEvent(event)
                .addOnSuccessListener(a -> success.setValue(true))
                .addOnFailureListener(e -> messageResId.setValue(R.string.error_creating_event));

    }




}
