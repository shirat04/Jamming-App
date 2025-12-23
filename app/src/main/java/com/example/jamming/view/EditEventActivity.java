package com.example.jamming.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private String eventId;
    private FirebaseFirestore db;
    private ImageButton btnOpenMap;


    private EditText etEventTitle, etEventDescription, etEventLocation, etEventDate, etEventGenre, etEventTime, etEventCapacity;
    private Button btnSaveEvent, btnDeleteEvent;
    private boolean locationVerified = false;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";


    private Calendar selectedDateTime = Calendar.getInstance();
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            Intent data = result.getData();
                            selectedLat = data.getDoubleExtra("lat", 0.0);
                            selectedLng = data.getDoubleExtra("lng", 0.0);
                            selectedAddress = data.getStringExtra("address");

                            locationVerified = true;
                            etEventLocation.setText(selectedAddress);
                            etEventLocation.setError(null);
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        db = FirebaseFirestore.getInstance();

        initViews();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EVENT_ID")) {
            eventId = intent.getStringExtra("EVENT_ID");
            if (eventId != null && !eventId.isEmpty()) {
                loadEventData(eventId);
            }
        }
        setupListeners();
    }

    private void initViews() {
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventCapacity = findViewById(R.id.etEventCapacity);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        etEventGenre = findViewById(R.id.editMusicgenre);
        btnOpenMap = findViewById(R.id.btnOpenMap);
    }

    private void setupListeners() {
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEventChanges());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());
        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        etEventLocation.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locationVerified = false;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadEventData(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            etEventTitle.setText(event.getName());
                            etEventDescription.setText(event.getDescription());
                            selectedLat = event.getLatitude();
                            selectedLng = event.getLongitude();
                            selectedAddress = event.getAddress();
                            locationVerified = true;

                            etEventLocation.setText(selectedAddress);
                            etEventCapacity.setText(String.valueOf(event.getMaxCapacity()));
                            List<String> genres = event.getMusicTypes();

                            if (genres != null && !genres.isEmpty()) {
                                etEventGenre.setText(String.join(", ", genres));
                            } else {
                                etEventGenre.setText("");
                            }

                            selectedDateTime.setTimeInMillis(event.getDateTime());
                            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            etEventDate.setText(sdfDate.format(selectedDateTime.getTime()));

                            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            etEventTime.setText(sdfTime.format(selectedDateTime.getTime()));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditEventActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker() {
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDateTime.set(Calendar.YEAR, y);
            selectedDateTime.set(Calendar.MONTH, m);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, d);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etEventDate.setText(sdf.format(selectedDateTime.getTime()));
            etEventDate.setError(null);
        }, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, h, m) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
            selectedDateTime.set(Calendar.MINUTE, m);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etEventTime.setText(sdf.format(selectedDateTime.getTime()));
            etEventTime.setError(null);
        }, hour, minute, true).show();
    }

    private void saveEventChanges() {
        String title = etEventTitle.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String capacityStr = etEventCapacity.getText().toString().trim();
        String genreMusic = etEventGenre.getText().toString().trim();


        if (title.isEmpty()) {
            etEventTitle.setError("נא להזין שם אירוע");
            etEventTitle.requestFocus();
            return;
        }
        if (description.isEmpty()){
            etEventDescription.setError("נא להזין תיאור האירוע");
            etEventDescription.requestFocus();
            return;
        }
        if (genreMusic.isEmpty()) {
            etEventGenre.setError("נא להזין ז'אנר");
            etEventGenre.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            etEventLocation.setError("נא להזין את מיקום האירוע");
            etEventLocation.requestFocus();
            return;
        }

        if (etEventDate.getText().toString().isEmpty()) {
            etEventDate.setError("נא להזין את תאריך האירוע");
            etEventDate.requestFocus();
            return;
        }
        if (etEventTime.getText().toString().isEmpty()){
            etEventTime.setError("נא להזין את שעת האירוע");
            etEventTime.requestFocus();
            return;
        }

        if (!verifyLocationIfNeeded(location)) {
            return;
        }


        if (capacityStr.isEmpty()) {
            etEventCapacity.setError("נא להזין מספר מקומות");
            etEventCapacity.requestFocus();
            return;
        }

        try {
            int cap = Integer.parseInt(capacityStr);
            if (cap <= 0) {
                etEventCapacity.setError("מספר המקומות חייב להיות חיובי");
                etEventCapacity.requestFocus();
                return;
            }
        } catch (Exception e) {
            etEventCapacity.setError("נא להזין מספר תקין");
            etEventCapacity.requestFocus();
            return;
        }


        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("name", title);
        updatedEvent.put("description", description);
        updatedEvent.put("address", selectedAddress);
        updatedEvent.put("latitude", selectedLat);
        updatedEvent.put("longitude", selectedLng);
        updatedEvent.put("maxCapacity", Integer.parseInt(capacityStr));
        updatedEvent.put("dateTime", selectedDateTime.getTimeInMillis());
        List<String> genres =
                List.of(genreMusic.split("\\s*,\\s*"));
        updatedEvent.put("musicTypes", genres);


        db.collection("events").document(eventId).update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2001 && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0.0);
            selectedLng = data.getDoubleExtra("lng", 0.0);
            selectedAddress = data.getStringExtra("address");

            locationVerified = true;
            etEventLocation.setText(selectedAddress);
            etEventLocation.setError(null);
        }
    }
    private boolean verifyLocationIfNeeded(String locationText) {
        if (locationText.isEmpty()) {
            etEventLocation.setError("נא להזין מיקום");
            etEventLocation.requestFocus();
            return false;
        }

        if (locationVerified) return true;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results =
                    geocoder.getFromLocationName(locationText, 1);

            if (results != null && !results.isEmpty()) {
                Address addr = results.get(0);

                String street = addr.getThoroughfare();
                String house = addr.getSubThoroughfare();
                boolean missingStreet = (street == null || street.trim().isEmpty());
                boolean missingHouse  = (house == null  || house.trim().isEmpty());


                if (missingStreet || missingHouse) {
                    etEventLocation.setError("נא להזין כתובת מדויקת (רחוב ומספר) או לבחור מהמפה");
                    etEventLocation.requestFocus();
                    return false;
                }

                selectedLat = addr.getLatitude();
                selectedLng = addr.getLongitude();
                selectedAddress = addr.getAddressLine(0);
                locationVerified = true;
                etEventLocation.setError(null);
                return true;

            } else {
                etEventLocation.setError("הכתובת לא נמצאה. נא לבחור מהמפה");
                etEventLocation.requestFocus();
                return false;
            }

        } catch (IOException e) {
            etEventLocation.setError("לא ניתן לאמת כתובת. נא לבחור מהמפה");
            etEventLocation.requestFocus();
            return false;
        }
    }

    private void deleteEvent() {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                });
    }
}
