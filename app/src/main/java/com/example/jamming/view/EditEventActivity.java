package com.example.jamming.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.viewmodel.EditEventViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class EditEventActivity extends BaseActivity {

    private EditText etTitle, etDescription, etLocation, etDate, etTime, etCapacity;
    private TextView genreText;
    private Button btnSave, btnDelete;
    private ImageButton btnMap;

    private EditEventViewModel viewModel;
    private String eventId;
    private Calendar c;

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            double lat = result.getData().getDoubleExtra("lat", 0);
                            double lng = result.getData().getDoubleExtra("lng", 0);
                            String address = result.getData().getStringExtra("address");

                            viewModel.onLocationSelected(lat, lng, address);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_edit_event
        );
        setTitleText(getString(R.string.edit_event));
        hideRightActions();

        viewModel = new ViewModelProvider(this).get(EditEventViewModel.class);
        eventId = getIntent().getStringExtra("EVENT_ID");
        c = viewModel.getDateTime();

        initViews();
        observeViewModel();
        setupListeners();

        if (eventId != null && !eventId.trim().isEmpty()) {
            viewModel.loadEvent(eventId);
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etLocation = findViewById(R.id.etEventLocation);
        etDate = findViewById(R.id.etEventDate);
        etTime = findViewById(R.id.etEventTime);
        etCapacity = findViewById(R.id.etEventCapacity);
        genreText = findViewById(R.id.selectGenreText);
        btnSave = findViewById(R.id.btnSaveEvent);
        btnDelete = findViewById(R.id.btnDeleteEvent);
        btnMap = findViewById(R.id.btnOpenMap);
    }

    private void observeViewModel() {
        viewModel.getTitle().observe(this, text -> setIfDifferent(etTitle, text));
        viewModel.getDescription().observe(this, text -> setIfDifferent(etDescription, text));
        viewModel.getLocationText().observe(this, text -> setIfDifferent(etLocation, text));
        viewModel.getDateText().observe(this, text -> setIfDifferent(etDate, text));
        viewModel.getTimeText().observe(this, text -> setIfDifferent(etTime, text));
        viewModel.getCapacityText().observe(this, text -> setIfDifferent(etCapacity, text));
        viewModel.getGenresText().observe(this, text -> {
            if (text == null || text.trim().isEmpty()) {
                genreText.setText("");
            } else {
                genreText.setText(text);
                genreText.setError(null);
            }
        });

        viewModel.getErrorField().observe(this, field -> {
            if (field == null) {
                genreText.setError(null);
                etTitle.setError(null);
                etDescription.setError(null);
                etLocation.setError(null);
                etDate.setError(null);
                etTime.setError(null);
                etCapacity.setError(null);
                return;
            }
            switch (field) {
                case TITLE:
                    etTitle.setError("נא להזין שם אירוע");
                    etTitle.requestFocus();
                    break;

                case DESCRIPTION:
                    etDescription.setError("נא להזין תיאור");
                    etDescription.requestFocus();
                    break;

                case LOCATION:
                    etLocation.setError("נא לבחור מיקום");
                    etLocation.requestFocus();
                    break;

                case DATE:
                    etDate.setError("נא לבחור תאריך");
                    etDate.requestFocus();
                    break;

                case TIME:
                    etTime.setError("נא לבחור שעה");
                    etTime.requestFocus();
                    break;

                case CAPACITY:
                    etCapacity.setError("קיבולת לא תקינה");
                    etCapacity.requestFocus();
                    break;

                case GENRE:
                    genreText.setError("נא לבחור ז'אנר");
                    genreText.requestFocus();
                    break;
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void setupListeners() {
        etTitle.addTextChangedListener(new SimpleTextWatcher(s -> {
            viewModel.onTitleChanged(s);
        }));

        etDescription.addTextChangedListener(new SimpleTextWatcher(s -> {
            viewModel.onDescriptionChanged(s);
        }));

        etCapacity.addTextChangedListener(new SimpleTextWatcher(s -> {
            viewModel.onCapacityChanged(s);
        }));

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnMap.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class))
        );
        etLocation.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class))
        );

        btnSave.setOnClickListener(v -> viewModel.saveChanges(eventId));
        btnDelete.setOnClickListener(v -> viewModel.deleteEvent(eventId));

        genreText.setOnClickListener(v -> openGenreDialog());
    }

    private void openGenreDialog() {
        String[] genres = getResources().getStringArray(R.array.music_genres);
        boolean[] checked = viewModel.getCheckedGenres(genres);

        new AlertDialog.Builder(this)
                .setTitle("Select music genres")
                .setMultiChoiceItems(genres, checked,
                        (dialog, which, isChecked) -> viewModel.toggleGenre(genres[which], isChecked)
                )
                .setPositiveButton("OK", (d, w) -> {
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, y, m, d) -> {
                    viewModel.setDate(y, m, d);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this,
                (view, h, m) -> {
                    viewModel.setTime(h, m);
                    etTime.setError(null);
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void setIfDifferent(EditText et, String value) {
        String v = value == null ? "" : value;
        if (!v.equals(et.getText().toString())) {
            et.setText(v);
        }
    }

    private static class SimpleTextWatcher implements TextWatcher {
        interface OnChanged { void run(String s); }
        private final OnChanged onChanged;

        SimpleTextWatcher(OnChanged onChanged) { this.onChanged = onChanged; }

        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
            onChanged.run(s == null ? "" : s.toString());
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}
