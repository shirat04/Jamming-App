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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateNewEventViewModel extends ViewModel {
    private AuthRepository authRepo;

    private EventRepository eventRepository;
    public CreateNewEventViewModel() {
        this(new AuthRepository(), new EventRepository());
    }

    public CreateNewEventViewModel(AuthRepository authRepo,
                                   EventRepository eventRepository) {
        this.authRepo = authRepo;
        this.eventRepository = eventRepository;
    }
    private final Calendar dateTime = Calendar.getInstance();

    private double lat, lng;
    private String address;
    private final List<MusicGenre> genres = new ArrayList<>();

    private final MutableLiveData<String> dateText = new MutableLiveData<>();
    private final MutableLiveData<String> timeText = new MutableLiveData<>();
    private final MutableLiveData<String> locationText = new MutableLiveData<>();
    private final MutableLiveData<String> genresText = new MutableLiveData<>("");
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<EventField> errorField = new MutableLiveData<>();


    public LiveData<EventField> getErrorField() { return errorField; }
    public LiveData<String> getDateText() { return dateText; }
    public LiveData<String> getTimeText() { return timeText; }
    public LiveData<String> getLocationText() { return locationText; }
    public LiveData<String> getGenresTextLive() {return genresText;}
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getSuccess() { return success; }
    public Calendar getDateTime() {return dateTime;}


    public String getGenresText() {
        List<String> names = new ArrayList<>();
        for (MusicGenre g : genres) {
            names.add(g.getDisplayName());
        }
        return String.join(" , ", names);
    }
    private List<String> getGenreStrings() {
        List<String> result = new ArrayList<>();
        for (MusicGenre g : genres) {
            result.add(g.getDisplayName());
        }
        return result;
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

    public void toggleGenre(MusicGenre genre, boolean checked) {
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



    public boolean[] getCheckedGenres(MusicGenre[] allGenres) {
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

        if (selectedTime <= now) {
            errorField.setValue(EventField.TIME);
            toastMessage.setValue("Cannot create an event for a time that has already passed.");
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
            toastMessage.setValue("שגיאת משתמש");
            return;
        }

        Event event = new Event(
                ownerId,
                name,
                description,
                getGenreStrings(),
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
