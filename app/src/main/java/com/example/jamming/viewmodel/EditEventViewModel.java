package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.model.EventField;
import com.example.jamming.utils.GenreUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel responsible for editing an existing event.
 *
 * This ViewModel manages:
 * - Loading event data from the repository
 * - Exposing editable fields to the UI via LiveData
 * - Validating user input
 * - Updating the event in Firestore
 *
 * The ViewModel does not hold any reference to UI components
 * and follows the MVVM architecture principles.
 */
public class EditEventViewModel extends ViewModel {

    private final EventRepository eventRepository;

    /**
     * Default constructor used in production.
     * Initializes the ViewModel with the default repository.
     */
    public EditEventViewModel() {
        this(new EventRepository());
    }

    /**
     * Constructor for dependency injection.
     * Allows passing a mock repository for testing.
     */
    public EditEventViewModel(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /** Holds the selected date and time of the event */
    private final Calendar dateTime = Calendar.getInstance();

    private double lat, lng;
    private String address, eventId;

    /** Selected music genres */
    private final List<MusicGenre> genres = new ArrayList<>();

    /* ===== UI State (LiveData) ===== */
    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");
    private final MutableLiveData<String> locationText = new MutableLiveData<>("");
    private final MutableLiveData<String> dateText = new MutableLiveData<>("");
    private final MutableLiveData<String> timeText = new MutableLiveData<>("");
    private final MutableLiveData<String> capacityText = new MutableLiveData<>("");
    private final MutableLiveData<String> genresText = new MutableLiveData<>("");

    /** Indicates whether editing this event is still allowed (e.g., event not in the past) */
    private final MutableLiveData<Boolean> editingAllowed = new MutableLiveData<>(true);

    /** Validation and result feedback */
    private final MutableLiveData<EventField> errorField = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Integer> successMessageRes = new MutableLiveData<>();

    /* ===== LiveData getters exposed to the View ===== */
    public LiveData<Boolean> getEditingAllowed() { return editingAllowed; }
    public LiveData<Integer> getSuccessMessageRes() { return successMessageRes; }
    public LiveData<String> getTitle() { return title; }
    public LiveData<String> getDescription() { return description; }
    public LiveData<String> getLocationText() { return locationText; }
    public LiveData<String> getDateText() { return dateText; }
    public LiveData<String> getTimeText() { return timeText; }
    public LiveData<String> getCapacityText() { return capacityText; }
    public LiveData<String> getGenresText() { return genresText; }
    public Calendar getDateTime() { return dateTime; }
    public LiveData<EventField> getErrorField() { return errorField; }
    public LiveData<Integer> getErrorMessageRes() { return errorMessageRes; }

    private int currentReserved = 0;


    /**
     * Loads an event from the repository and initializes the ViewModel state.
     */
    private void loadEvent(String eventId) {
        this.eventId = eventId;
        eventRepository.getEventById(eventId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        errorMessageRes.setValue(R.string.error_failed_to_load_event);
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event == null) {
                        errorMessageRes.setValue(R.string.error_failed_to_load_event);
                        return;
                    }

                    currentReserved = event.getReserved();

                    // Determine whether editing is still allowed
                    editingAllowed.setValue(isEditingAllowed(event));

                    // Populate editable fields
                    title.setValue(event.getName());
                    description.setValue(event.getDescription());
                    capacityText.setValue(String.valueOf(event.getMaxCapacity()));

                    address = event.getAddress();
                    lat = event.getLatitude();
                    lng = event.getLongitude();
                    locationText.setValue(address);

                    // Load and convert genres
                    genres.clear();
                    if (event.getMusicTypes() != null) {
                        for (String s : event.getMusicTypes()) {
                            try {
                                genres.add(MusicGenre.fromDisplayName(s));
                            } catch (IllegalArgumentException ignored) {
                                // Ignore unknown genres
                            }
                        }
                    }
                    genresText.setValue(GenreUtils.genresToTextFromEnums(genres));

                    // Initialize date and time
                    dateTime.setTimeInMillis(event.getDateTime());
                    dateText.setValue(DateUtils.formatOnlyDate(event.getDateTime()));
                    timeText.setValue(DateUtils.formatOnlyTime(event.getDateTime()));
                })
                .addOnFailureListener(e ->
                        errorMessageRes.setValue(R.string.error_loading_event_he));
    }

    /**
     * Initializes the ViewModel with an event ID.
     * Ensures the event is loaded only once.
     */
    public void init(String eventId) {
        if (this.eventId != null) return;

        if (eventId != null && !eventId.trim().isEmpty()) {
            loadEvent(eventId);
        }
    }

    /* ===== Location handling ===== */

    public void onLocationSelected(double lat, double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        locationText.setValue(address == null ? "" : address);

        // Clear location error once valid input is provided
        if (address != null && !address.trim().isEmpty()) {
            if (errorField.getValue() == EventField.LOCATION) errorField.setValue(null);
        }
    }

    /* ===== Date & Time handling ===== */

    public void setDate(int year, int month, int day) {
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month);
        dateTime.set(Calendar.DAY_OF_MONTH, day);
        dateText.setValue(DateUtils.formatOnlyDate(dateTime.getTimeInMillis()));
        if (errorField.getValue() == EventField.DATE) errorField.setValue(null);
    }

    public void setTime(int hour, int minute) {
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        timeText.setValue(DateUtils.formatOnlyTime(dateTime.getTimeInMillis()));
        if (errorField.getValue() == EventField.TIME) errorField.setValue(null);
    }

    /* ===== Genre handling ===== */

    public void toggleGenre(MusicGenre genre, boolean checked) {
        if (genre == null) return;

        if (checked && !genres.contains(genre)) {
            genres.add(genre);
        } else if (!checked) {
            genres.remove(genre);
        }

        genresText.setValue(GenreUtils.genresToTextFromEnums(genres));

        // Clear genre validation error if at least one genre is selected
        if (!genres.isEmpty() && errorField.getValue() == EventField.GENRE) {
            errorField.setValue(null);
        }
    }

    /**
     * Returns a boolean array representing which genres are selected.
     * Used for restoring UI selection state.
     */
    public boolean[] getCheckedGenres(MusicGenre[] allGenres) {
        boolean[] checked = new boolean[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            checked[i] = genres.contains(allGenres[i]);
        }
        return checked;
    }

    /**
     * Determines whether editing the given event is still allowed.
     * Editing is blocked if the event time is already in the past.
     */
    private boolean isEditingAllowed(Event event) {
        return event.getDateTime() >= System.currentTimeMillis();
    }

    /* ===== Save changes ===== */

    /**
     * Validates all input fields and updates the event in the repository.
     * Reports validation errors using EventField for precise UI feedback.
     */
    public void saveChanges() {
        errorField.setValue(null);

        if (eventId == null) {
            errorMessageRes.setValue(R.string.error_missing_event_id);
            return;
        }

        if (!Boolean.TRUE.equals(editingAllowed.getValue())) {
            errorMessageRes.setValue(R.string.error_edit_past_event);
            return;
        }

        String t = title.getValue() == null ? "" : title.getValue().trim();
        String d = description.getValue() == null ? "" : description.getValue().trim();
        String capStr = capacityText.getValue() == null ? "" : capacityText.getValue().trim();

        if (t.isEmpty()) { errorField.setValue(EventField.TITLE); return; }
        if (d.isEmpty()) { errorField.setValue(EventField.DESCRIPTION); return; }
        if (address == null || address.trim().isEmpty()) { errorField.setValue(EventField.LOCATION); return; }
        if (dateText.getValue() == null || dateText.getValue().isEmpty()) { errorField.setValue(EventField.DATE); return; }
        if (timeText.getValue() == null || timeText.getValue().isEmpty()) { errorField.setValue(EventField.TIME); return; }

        long now = System.currentTimeMillis();
        long selectedTime = dateTime.getTimeInMillis();

        if (selectedTime <= now) {
            errorField.setValue(EventField.TIME);
            errorMessageRes.setValue(R.string.error_event_time_already_passed);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capStr);
            if (cap <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            errorField.setValue(EventField.CAPACITY);
            return;
        }
        if (cap < currentReserved) {
            errorField.setValue(EventField.CAPACITY);
            errorMessageRes.setValue(R.string.error_capacity_too_small);
            return;
        }

        if (genres.isEmpty()) { errorField.setValue(EventField.GENRE); return; }

        // Build update map
        List<String> genreStrings = GenreUtils.genresToStrings(genres);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", t);
        updates.put("description", d);
        updates.put("address", address);
        updates.put("latitude", lat);
        updates.put("longitude", lng);
        updates.put("maxCapacity", cap);
        updates.put("dateTime", dateTime.getTimeInMillis());
        updates.put("musicTypes", genreStrings);

        eventRepository.updateEvent(eventId, updates)
                .addOnSuccessListener(a ->
                        successMessageRes.setValue(R.string.event_updated_success))
                .addOnFailureListener(e ->
                        errorMessageRes.setValue(R.string.error_updating_event_he));
    }

    /**
     * Updates the event title in the ViewModel.
     *
     * @param title New event title.
     */
    public void setTitle(String title) {
        this.title.setValue(title);
    }

    /**
     * Updates the event description in the ViewModel.
     *
     * @param description New event description.
     */
    public void setDescription(String description) {
        this.description.setValue(description);
    }

    /**
     * Updates the capacity text in the ViewModel.
     *
     * @param capacityText New capacity value as text.
     */
    public void setCapacityText(String capacityText) {
        this.capacityText.setValue(capacityText);
    }
}
