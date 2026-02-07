package com.example.jamming.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.OwnerMenuHandler;
import com.example.jamming.utils.FormTextWatcher;
import com.example.jamming.viewmodel.EditEventViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.Calendar;

/**
 * Activity responsible for editing an existing event.
 * Handles UI input, delegates validation and business logic to EditEventViewModel,
 * and updates the screen using LiveData observers (MVVM architecture).
 */
public class EditEventActivity extends BaseActivity {

    // UI fields for event editing
    private EditText etTitle, etDescription, etLocation, etDate, etTime, etCapacity;
    private TextView genreText, cancelText;
    private Button btnSave;
    private ImageButton btnMap;

    // ViewModel and helpers
    private EditEventViewModel viewModel;
    private Calendar calendar;
    private OwnerMenuHandler menuHandler;

    /**
     * Launcher used to open the map picker activity and receive the selected location.
     */
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

    /**
     * Initializes the activity, loads the event ID, sets up the ViewModel,
     * and binds UI components, observers, and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_edit_event
        );
        setTitleText(getString(R.string.edit_event));
        hideRightActions();

        // Initialize ViewModel and menu handler
        viewModel = new ViewModelProvider(this).get(EditEventViewModel.class);
        menuHandler = new OwnerMenuHandler(this);

        // Validate that an event ID was provided
        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_missing_event_id), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load event data into the ViewModel
        viewModel.init(eventId);
        calendar = viewModel.getDateTime();

        // Initialize UI and bind logic
        initViews();
        observeViewModel();
        setupListeners();
    }

    /**
     * Finds and caches all required view references from the layout.
     */
    private void initViews() {
        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etLocation = findViewById(R.id.etEventLocation);
        etDate = findViewById(R.id.etEventDate);
        etTime = findViewById(R.id.etEventTime);
        etCapacity = findViewById(R.id.etEventCapacity);
        genreText = findViewById(R.id.selectGenreText);
        btnSave = findViewById(R.id.btnSaveEvent);
        cancelText = findViewById(R.id.btnCancelEvent);
        btnMap = findViewById(R.id.btnOpenMap);
    }

    /**
     * Observes LiveData from the ViewModel and updates the UI accordingly:
     * - Fills input fields
     * - Shows validation errors
     * - Displays messages and closes the screen on success
     */
    private void observeViewModel() {
        // Observe event fields from the ViewModel and keep the input fields in sync with the current event data
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

        // Observe validation errors and highlight the corresponding input field
        viewModel.getErrorField().observe(this, field -> {
            if (field == null) {
                clearErrors();
                return;
            }

            // Show validation error on the relevant field
            switch (field) {
                case TITLE:
                    etTitle.setError(getString(R.string.error_event_title));
                    etTitle.requestFocus();
                    break;
                case DESCRIPTION:
                    etDescription.setError(getString(R.string.error_event_description));
                    etDescription.requestFocus();
                    break;
                case LOCATION:
                    etLocation.setError(getString(R.string.error_event_location));
                    etLocation.requestFocus();
                    break;
                case DATE:
                    etDate.setError(getString(R.string.error_event_date));
                    etDate.requestFocus();
                    break;
                case TIME:
                    etTime.setError(getString(R.string.error_event_time));
                    etTime.requestFocus();
                    break;
                case CAPACITY:
                    etCapacity.setError(getString(R.string.error_event_capacity));
                    etCapacity.requestFocus();
                    break;
                case GENRE:
                    genreText.setError(getString(R.string.error_event_genre));
                    genreText.requestFocus();
                    break;
            }

        });

        // Observe general error messages (string resource IDs) and show them as a Snackbar
        viewModel.getErrorMessageRes().observe(this, resId -> {
            if (resId != null) {
                Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(resId),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });

        // Observe success messages (string resource IDs) and close the screen
        viewModel.getSuccessMessageRes().observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        // Observe whether editing is allowed (e.g., prevent editing past events)
        viewModel.getEditingAllowed().observe(this, allowed -> {
            if (allowed != null && !allowed) {
                Toast.makeText(this, getString(R.string.error_edit_past_event), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Clears all validation errors from input fields.
     */
    private void clearErrors() {
        genreText.setError(null);
        etTitle.setError(null);
        etDescription.setError(null);
        etLocation.setError(null);
        etDate.setError(null);
        etTime.setError(null);
        etCapacity.setError(null);
    }

    /**
     * Sets up all UI event listeners and delegates actions to the ViewModel.
     */
    private void setupListeners() {
        // Clear field errors when the user edits text
        etTitle.addTextChangedListener(FormTextWatcher.after(s -> etTitle.setError(null)));
        etDescription.addTextChangedListener(FormTextWatcher.after(s -> etDescription.setError(null)));
        etCapacity.addTextChangedListener(FormTextWatcher.after(s -> etCapacity.setError(null)));

        // Open date and time pickers
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // Open map picker for selecting a location
        btnMap.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class))
        );
        etLocation.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class))
        );
        // Open genre selection dialog
        genreText.setOnClickListener(v -> openGenreDialog());

        // Save or cancel actions
        btnSave.setOnClickListener(v -> viewModel.saveChanges());
        cancelText.setOnClickListener(v -> finish());

    }


    /**
     * Opens a multi-choice dialog for selecting music genres.
     */
    private void openGenreDialog() {
        MusicGenre[] allGenres = MusicGenre.values();
        String[] displayNames = new String[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            displayNames[i] = allGenres[i].getDisplayName();
        }
        boolean[] checked = viewModel.getCheckedGenres(allGenres);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_music_genres))
                .setMultiChoiceItems(
                        displayNames,
                        checked,
                        (dialog, which, isChecked) ->
                                viewModel.toggleGenre(allGenres[which], isChecked)
                )
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Shows a date picker dialog and updates the ViewModel with the selected date.
     */
    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, y, m, d) -> {
                    viewModel.setDate(y, m, d);
                    etDate.setError(null);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Shows a time picker dialog and updates the ViewModel with the selected time.
     */
    private void showTimePicker() {
        new TimePickerDialog(this,
                (view, h, m) -> {
                    viewModel.setTime(h, m);
                    etTime.setError(null);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    /**
     * Updates an EditText only if the new value is different,
     * to avoid unnecessary text resets and cursor jumps.
     */
    private void setIfDifferent(EditText et, String value) {
        String v = value == null ? "" : value;
        if (!v.equals(et.getText().toString())) {
            et.setText(v);
        }
    }

    /**
     * Handles navigation drawer menu item selections.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }
}
