package com.example.jamming.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.utils.AddressUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private String eventId;
    private FirebaseFirestore db;
    private ImageButton btnOpenMap;


    private EditText etEventTitle, etEventDescription, etEventLocation, etEventDate, etEventTime, etEventCapacity;
    private Button btnSaveEvent, btnDeleteEvent;
    private TextView selectGenreText;
    private boolean[] checkedGenres;
    private final List<String> selectedGenres = new ArrayList<>();
    private String[] allGenres;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";

    private final Calendar selectedDateTime = Calendar.getInstance();
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            Intent data = result.getData();
                            selectedLat = data.getDoubleExtra("lat", 0.0);
                            selectedLng = data.getDoubleExtra("lng", 0.0);
                            selectedAddress = data.getStringExtra("address");
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
        btnOpenMap = findViewById(R.id.btnOpenMap);
        selectGenreText = findViewById(R.id.selectGenreText);

    }

    private void setupListeners() {
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEventChanges());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());

        View.OnClickListener openMapListener = v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        };

        btnOpenMap.setOnClickListener(openMapListener);
        etEventLocation.setOnClickListener(openMapListener);

        allGenres = getResources().getStringArray(R.array.music_genres);
        checkedGenres = new boolean[allGenres.length];

        selectGenreText.setOnClickListener(v -> openGenreDialog());
    }

    private void openGenreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select music genres");

        builder.setMultiChoiceItems(allGenres, checkedGenres,
                (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedGenres.contains(allGenres[which])) {
                            selectedGenres.add(allGenres[which]);
                        }
                    } else {
                        selectedGenres.remove(allGenres[which]);
                    }
                });

        builder.setPositiveButton("OK", (dialog, which) -> {
            selectGenreText.setText(
                    android.text.TextUtils.join(" / ", selectedGenres)
            );
            selectGenreText.setError(null);

        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
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
                            etEventLocation.setText(selectedAddress);
                            etEventCapacity.setText(String.valueOf(event.getMaxCapacity()));
                            List<String> genres = event.getMusicTypes();

                            selectedGenres.clear();
                            selectedGenres.addAll(genres);

                            for (int i = 0; i < allGenres.length; i++) {
                                checkedGenres[i] = genres.contains(allGenres[i]);
                            }
                            selectGenreText.setText(
                                    android.text.TextUtils.join(" , ", genres)
                            );

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
        if (selectedGenres == null || selectedGenres.isEmpty()) {
            selectGenreText.setError("נא להזין ז'אנר");
            selectGenreText.requestFocus();
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

        if (selectedAddress == null || selectedAddress.trim().isEmpty()
                || Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {

            etEventLocation.setError("נא לבחור מיקום מהמפה");
            etEventLocation.requestFocus();
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
        updatedEvent.put("musicTypes", selectedGenres);


        db.collection("events").document(eventId).update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating event.", Toast.LENGTH_SHORT).show();
                });
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
