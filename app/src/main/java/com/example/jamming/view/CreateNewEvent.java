package com.example.jamming.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.example.jamming.model.Event;
import com.example.jamming.model.Owner;
import com.example.jamming.repository.EventRepository;
import com.example.jamming.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateNewEvent extends AppCompatActivity {

    private EditText eventNameInput, genreInput, capacityInput, descriptionInput, dateInput, timeInput,locationInput;
    private boolean locationVerified = false;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";
    private ImageButton mapButton;

    private Button publishBtn;
    private TextView cancelBtn;


    private Calendar selectedDateTime = Calendar.getInstance();

    private FirebaseAuth auth;
    private UserRepository userRepository;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        eventRepository = new EventRepository();
        initViews();


        setupListeners();
    }

    private void initViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        genreInput = findViewById(R.id.genreSpinner);
        capacityInput = findViewById(R.id.eventCapacityInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        publishBtn = findViewById(R.id.publishEventBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        locationInput = findViewById(R.id.eventLocationInput);
        mapButton = findViewById(R.id.mapButton);

    }

    private void setupListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setOnClickListener(v -> showTimePicker());
        publishBtn.setOnClickListener(v -> publishEvent());
        cancelBtn.setOnClickListener(v -> finish());

        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, 1001);
        });

        locationInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                locationVerified = false;
                selectedAddress = "";
                selectedLat = 0.0;
                selectedLng = 0.0;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        locationInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                Drawable drawableEnd =
                        locationInput.getCompoundDrawablesRelative()[2];

                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();

                    // בדיקה אם הלחיצה הייתה על האייקון בלבד
                    if (event.getRawX() >=
                            (locationInput.getRight() - drawableWidth)) {

                        Intent intent = new Intent(this, MapPickerActivity.class);
                        startActivityForResult(intent, 1001);
                        return true; // חשוב! מונע המשך טיפול
                    }
                }
            }
            return false; // מאפשר הקלדה רגילה
        });

    }
    private boolean verifyLocationIfNeeded(String locationText) {
        if (locationVerified) return true;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results =
                    geocoder.getFromLocationName(locationText, 1);

            if (results != null && !results.isEmpty()) {
                Address addr = results.get(0);
                selectedLat = addr.getLatitude();
                selectedLng = addr.getLongitude();
                selectedAddress = addr.getAddressLine(0);
                locationVerified = true;
                locationInput.setError(null);
                return true;
            } else {
                locationInput.setError("הכתובת לא נמצאה. נא לבחור מהמפה");
                locationInput.requestFocus();
                return false;
            }

        } catch (IOException e) {
            locationInput.setError("לא ניתן לאמת כתובת. נא לבחור מהמפה");
            locationInput.requestFocus();
            return false;
        }
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
            dateInput.setText(sdf.format(selectedDateTime.getTime()));
            dateInput.setError(null);
        }, year, month, day);

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();


    }

    private void showTimePicker() {
        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDateTime.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
            selectedDateTime.set(Calendar.MINUTE, m);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeInput.setText(sdf.format(selectedDateTime.getTime()));
            timeInput.setError(null);
        }, hour, minute, true);

        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0.0);
            selectedLng = data.getDoubleExtra("lng", 0.0);
            selectedAddress = data.getStringExtra("address");
            locationVerified = true;
            locationInput.setText(selectedAddress);
        }
    }

    private void publishEvent() {

        String name = eventNameInput.getText().toString().trim();
        String genreStr = genreInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();


        if (!validateInputs(name, genreStr, capacityStr, location)) {
            return;
        }
        if (!verifyLocationIfNeeded(location)) {
            return;
        }

        int capacity = Integer.parseInt(capacityStr);
        List<String> musicTypes = Arrays.asList(genreStr.split(","));

        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (ownerId == null) {
            Toast.makeText(this, "שגיאה בזיהוי בעל האירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getUserById(ownerId).addOnSuccessListener(doc -> {
            Owner owner = doc.toObject(Owner.class);

            if (owner == null) {
                Toast.makeText(this, "שגיאה בפרטי הבעלים", Toast.LENGTH_SHORT).show();
                return;
            }


            Event event = new Event(
                    ownerId,
                    name,
                    description,
                    musicTypes,
                    selectedAddress,
                    selectedDateTime.getTimeInMillis(),
                    capacity,
                    selectedLat,
                    selectedLng
            );


            eventRepository.createEvent(event)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "האירוע נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בטעינת פרטי הבעלים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInputs(String name, String genre, String capacity ,String location) {

        if (name.isEmpty()) {
            eventNameInput.setError("נא להזין שם אירוע");
            eventNameInput.requestFocus();
            return false;
        }
        if (location.isEmpty()) {
            locationInput.setError("נא להזין מיקום לאירוע");
            locationInput.requestFocus();
            return false;
        }
        if (dateInput.getText().toString().isEmpty()) {
            dateInput.setError("נא להזין את תאריך האירוע");
            dateInput.requestFocus();
            return false;
        }
        if( timeInput.getText().toString().isEmpty()){
            timeInput.setError("נא להזין את שעת האירוע");
            timeInput.requestFocus();
            return false;
        }

        if (genre.isEmpty()) {
            genreInput.setError("נא להזין ז'אנר");
            genreInput.requestFocus();
            return false;
        }

        if (capacity.isEmpty()) {
            capacityInput.setError("נא להזין מספר מקומות");
            capacityInput.requestFocus();
            return false;
        }

        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) {
                capacityInput.setError("מספר המקומות חייב להיות חיובי");
                capacityInput.requestFocus();
                return false;
            }
        } catch (Exception e) {
            capacityInput.setError("נא להזין מספר תקין");
            capacityInput.requestFocus();
            return false;
        }

        return true;
    }
}
