package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.Event;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateNewEventViewModel extends ViewModel {
    private final AuthRepository authRepo = new AuthRepository();

    private final EventRepository eventRepository = new EventRepository();

    private final Calendar dateTime = Calendar.getInstance();

    private double lat, lng;
    private String address;
    private final List<String> genres = new ArrayList<>();

    private final MutableLiveData<String> dateText = new MutableLiveData<>();
    private final MutableLiveData<String> timeText = new MutableLiveData<>();
    private final MutableLiveData<String> locationText = new MutableLiveData<>();

    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> locationError = new MutableLiveData<>();
    private final MutableLiveData<String> capacityError = new MutableLiveData<>();
    private final MutableLiveData<String> dateError = new MutableLiveData<>();
    private final MutableLiveData<String> timeError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> genreError = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<String> genresText = new MutableLiveData<>();
    public LiveData<Boolean> getGenreError() {return genreError;}
    public LiveData<String> getDateError() { return dateError; }
    public LiveData<String> getTimeError() { return timeError; }

    public LiveData<String> getDateText() { return dateText; }
    public LiveData<String> getTimeText() { return timeText; }
    public LiveData<String> getLocationText() { return locationText; }

    public LiveData<String> getNameError() { return nameError; }
    public LiveData<String> getLocationError() { return locationError; }
    public LiveData<String> getCapacityError() { return capacityError; }

    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getSuccess() { return success; }

    public String getGenresText() {
        return String.join(" , ", genres);
    }


    public void setDate(int year, int month, int day) {
        dateTime.set(year, month, day);
        dateText.setValue(
                DateUtils.formatOnlyDate(dateTime.getTimeInMillis())
        );
    }

    public void setTime(int hour, int minute) {
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        timeText.setValue(
                DateUtils.formatOnlyTime(dateTime.getTimeInMillis())
        );
    }

    public void toggleGenre(String genre, boolean checked) {
        if (checked && !genres.contains(genre)) {
            genres.add(genre);
            genresText.setValue(String.join(" , ", genres));
        } else if (!checked) {
            genres.remove(genre);
            genresText.setValue(String.join(" , ", genres));
        }
        genreError.setValue(genres.isEmpty());
    }


    public boolean[] getCheckedGenres(String[] allGenres) {
        boolean[] checked = new boolean[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            checked[i] = genres.contains(allGenres[i]);
        }
        return checked;
    }
    public void onLocationSelected(double lat, double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        locationText.setValue(address);
    }

    public void publish(String name, String capacity, String description) {
        boolean valid = true;

        if (name == null || name.trim().isEmpty()) {
            nameError.setValue("נא להזין שם אירוע");
            valid = false;
        } else {
            nameError.setValue(null);
        }

        if (dateText.getValue() == null) {
            dateError.setValue("נא לבחור תאריך");
            valid = false;
        } else {
            dateError.setValue(null);
        }

        if (timeText.getValue() == null) {
            timeError.setValue("נא לבחור שעה");
            valid = false;
        } else {
            timeError.setValue(null);
        }
        if (address == null || address.trim().isEmpty()) {
            locationError.setValue("נא לבחור מיקום");
            valid = false;
        } else {
            locationError.setValue(null);
        }

        int cap = 0;
        try {
            cap = Integer.parseInt(capacity);
            if (cap == 0){
                capacityError.setValue("נא להזין כמות משתתפים");
                valid = false;
            }
            if (cap < 0) throw new NumberFormatException();
        } catch (Exception e) {
            capacityError.setValue("נא להזין  כמות משתתפים תקינה");
            valid = false;
        }

        if (genres.isEmpty()) {
            genreError.setValue(true);
            valid = false;
        } else {
            genreError.setValue(false);
        }

        if (!valid) return;

        String ownerId = authRepo.getCurrentUid();
        if (ownerId == null) {
            toastMessage.setValue("שגיאת משתמש");
            return;
        }

        Event event = new Event(
                ownerId,
                name,
                description,
                new ArrayList<>(genres),
                address,
                dateTime.getTimeInMillis(),
                cap,
                lat,
                lng
        );

        eventRepository.createEvent(event)
                .addOnSuccessListener(a -> success.setValue(true))
                .addOnFailureListener(e ->
                        toastMessage.setValue(e.getMessage()));
    }
}
