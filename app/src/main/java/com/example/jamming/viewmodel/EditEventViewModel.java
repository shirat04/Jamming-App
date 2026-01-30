package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.jamming.model.Event;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;
import com.example.jamming.model.EventField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditEventViewModel extends ViewModel {

    private final EventRepository eventRepository = new EventRepository();

    private final Calendar dateTime = Calendar.getInstance();
    private double lat, lng;
    private String address, eventId;

    private final List<MusicGenre> genres = new ArrayList<>();

    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> description = new MutableLiveData<>("");
    private final MutableLiveData<String> locationText = new MutableLiveData<>("");
    private final MutableLiveData<String> dateText = new MutableLiveData<>("");
    private final MutableLiveData<String> timeText = new MutableLiveData<>("");
    private final MutableLiveData<String> capacityText = new MutableLiveData<>("");
    private final MutableLiveData<String> genresText = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> editingAllowed = new MutableLiveData<>(true);

    private final MutableLiveData<EventField> errorField = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    public LiveData<Boolean> getEditingAllowed() {return editingAllowed;}
    public LiveData<String> getSuccessMessage() { return successMessage; }

    private boolean isDateSet = false;
    private boolean isTimeSet = false;

    // ===== LiveData getters =====
    public LiveData<String> getTitle() { return title; }
    public LiveData<String> getDescription() { return description; }
    public LiveData<String> getLocationText() { return locationText; }
    public LiveData<String> getDateText() { return dateText; }
    public LiveData<String> getTimeText() { return timeText; }
    public LiveData<String> getCapacityText() { return capacityText; }
    public LiveData<String> getGenres() { return genresText; }
    public Calendar getDateTime() {return dateTime;}

    public LiveData<EventField> getErrorField() { return errorField; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // ===== Setters from UI (Activity) =====
    public void onTitleChanged(String v) { title.setValue(v == null ? "" : v); }
    public void onDescriptionChanged(String v) { description.setValue(v == null ? "" : v); }
    public void onCapacityChanged(String v) { capacityText.setValue(v == null ? "" : v); }

    // ===== Load event =====
    public void loadEvent(String eventId) {
        eventRepository.getEventById(eventId)
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        errorMessage.setValue("שגיאה בטעינת האירוע");
                        return;
                    }

                    Event event = doc.toObject(Event.class);
                    if (event == null) {
                        errorMessage.setValue("שגיאה בטעינת האירוע");
                        return;
                    }

                    if (event.getDateTime() < System.currentTimeMillis()) {
                        editingAllowed.setValue(false);
                        return;
                    }
                    title.setValue(event.getName());
                    description.setValue(event.getDescription());
                    capacityText.setValue(String.valueOf(event.getMaxCapacity()));

                    address = event.getAddress();
                    lat = event.getLatitude();
                    lng = event.getLongitude();
                    locationText.setValue(address);

                    genres.clear();
                    if (event.getMusicTypes() != null) {
                        for (String s : event.getMusicTypes()) {
                            try {
                                genres.add(MusicGenre.fromDisplayName(s));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    genresText.setValue(getGenresText());

                    dateTime.setTimeInMillis(event.getDateTime());
                    dateText.setValue(DateUtils.formatOnlyDate(event.getDateTime()));
                    timeText.setValue(DateUtils.formatOnlyTime(event.getDateTime()));

                    isDateSet = true;
                    isTimeSet = true;
                })
                .addOnFailureListener(e -> errorMessage.setValue("שגיאה בטעינת האירוע"));
    }
    public void init(String eventId) {
        if (this.eventId != null) return;
        this.eventId = eventId;

        if (eventId != null && !eventId.trim().isEmpty()) {
            loadEvent(eventId);
        }
    }

    // ===== Location =====
    public void onLocationSelected(double lat, double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        locationText.setValue(address == null ? "" : address);
        if (address != null && !address.trim().isEmpty()) {
            if (errorField.getValue() == EventField.LOCATION) errorField.setValue(null);
        }
    }

    // ===== Date & Time =====
    public void setDate(int year, int month, int day) {
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month);
        dateTime.set(Calendar.DAY_OF_MONTH, day);
        isDateSet = true;
        dateText.setValue(DateUtils.formatOnlyDate(dateTime.getTimeInMillis()));
        if (errorField.getValue() == EventField.DATE) errorField.setValue(null);
    }

    public void setTime(int hour, int minute) {
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        isTimeSet = true;
        timeText.setValue(DateUtils.formatOnlyTime(dateTime.getTimeInMillis()));
        if (errorField.getValue() == EventField.TIME) errorField.setValue(null);
    }

    // ===== Genres =====
    public void toggleGenre(MusicGenre genre, boolean checked) {
        if (genre == null) return;

        if (checked && !genres.contains(genre)) {
            genres.add(genre);
        } else if (!checked) {
            genres.remove(genre);
        }

        genresText.setValue(getGenresText());

        if (!genres.isEmpty() && errorField.getValue() == EventField.GENRE) {
            errorField.setValue(null);
        }
    }

    private String getGenresText() {
        List<String> names = new ArrayList<>();
        for (MusicGenre g : genres) {
            names.add(g.getDisplayName());
        }
        return String.join(" , ", names);
    }


    public boolean[] getCheckedGenres(MusicGenre[] allGenres) {
        boolean[] checked = new boolean[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            checked[i] = genres.contains(allGenres[i]);
        }
        return checked;
    }


    // ===== Save changes =====
    public void saveChanges() {
        errorField.setValue(null);
        if (eventId == null) {
            errorMessage.setValue("שגיאה: מזהה אירוע חסר");
            return;
        }
        String t = title.getValue() == null ? "" : title.getValue().trim();
        String d = description.getValue() == null ? "" : description.getValue().trim();
        String capStr = capacityText.getValue() == null ? "" : capacityText.getValue().trim();

        if (t.isEmpty()) { errorField.setValue(EventField.TITLE); return; }
        if (d.isEmpty()) { errorField.setValue(EventField.DESCRIPTION); return; }
        if (address == null || address.trim().isEmpty()) { errorField.setValue(EventField.LOCATION); return; }
        if (!isDateSet) { errorField.setValue(EventField.DATE); return; }
        if (!isTimeSet) { errorField.setValue(EventField.TIME); return; }

        int cap;
        try {
            cap = Integer.parseInt(capStr);
            if (cap <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            errorField.setValue(EventField.CAPACITY);
            return;
        }

        List<String> genreStrings = new ArrayList<>();
        for (MusicGenre g : genres) {
            genreStrings.add(g.getDisplayName());
        }
        if (genres.isEmpty()) { errorField.setValue(EventField.GENRE); return; }

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
                .addOnSuccessListener(a -> successMessage.setValue("האירוע עודכן בהצלחה"))
                .addOnFailureListener(e -> errorMessage.setValue("שגיאה בעדכון האירוע"));
    }

    // ===== Delete =====
    public void deleteEvent() {
        if (eventId == null) {
            errorMessage.setValue("שגיאה: מזהה אירוע חסר");
            return;
        }

        eventRepository.deleteEvent(eventId)
                .addOnSuccessListener(a ->
                        successMessage.setValue("האירוע נמחק בהצלחה")
                )
                .addOnFailureListener(e ->
                        errorMessage.setValue("שגיאה במחיקת האירוע")
                );
    }
}
