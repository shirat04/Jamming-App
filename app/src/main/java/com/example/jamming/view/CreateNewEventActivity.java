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
import com.example.jamming.R;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.navigation.OwnerMenuHandler;
import com.example.jamming.viewmodel.CreateNewEventViewModel;
import java.util.Calendar;

/**
 * Activity responsible for creating a new event.
 * Handles user input, shows date/time/genre pickers, validates input via ViewModel,
 * and publishes the event when all fields are valid.
 *
 * Follows MVVM: UI logic here, validation and business logic in CreateNewEventViewModel.
 */
public class CreateNewEventActivity extends BaseActivity {

    // UI elements
    private EditText nameInput, capacityInput, descriptionInput, locationInput;
    private TextView dateInput, timeInput, genreText;
    private Button publishBtn, cancelBtn;
    private ImageButton mapButton;

    // Calendar instance used for initializing date and time pickers
    private Calendar calendar;

    // Handles navigation drawer menu actions for owner screens
    private OwnerMenuHandler menuHandler;


    // ViewModel
    private CreateNewEventViewModel viewModel;

    /**
     * Launcher for opening the MapPickerActivity and receiving the selected location.
     * When a location is returned successfully, the result is forwarded to the ViewModel.
     */
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null && viewModel != null) {
                            viewModel.onLocationSelected(
                                    result.getData().getDoubleExtra("lat", 0),
                                    result.getData().getDoubleExtra("lng", 0),
                                    result.getData().getStringExtra("address")
                            );
                        }
                    });

    /**
     * Called when the Activity is created.
     * Initializes the base layout, ViewModel, helpers, UI references,
     * observers and event listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the base layout with owner menu and this screen's content
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_create_new_event
        );
        // Set the screen title and hide right action buttons
        setTitleText(getString(R.string.create_new_event));
        hideRightActions();

        // Initialize ViewModel and helpers
        viewModel = new ViewModelProvider(this).get(CreateNewEventViewModel.class);
        calendar = viewModel.getDateTime();
        menuHandler = new OwnerMenuHandler(this);

        // Initialize UI and bind logic
        initViews();
        observeViewModel();
        setupListeners();
    }

    /**
     * Finds and caches all required view references from the layout.
     */
    private void initViews() {
        nameInput = findViewById(R.id.eventNameInput);
        capacityInput = findViewById(R.id.eventCapacityInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        locationInput = findViewById(R.id.eventLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        genreText = findViewById(R.id.genreSpinner);
        publishBtn = findViewById(R.id.publishEventBtn);
        cancelBtn = findViewById(R.id.cancelText);
        mapButton = findViewById(R.id.mapButton);
    }

    /**
     * Observes ViewModel LiveData and updates the UI accordingly:
     * - Displays validation errors on relevant fields
     * - Updates date, time, location and genres text
     * - Shows toast messages
     * - Closes the screen on successful event creation
     */
    private void observeViewModel() {
        viewModel.getErrorField().observe(this, field -> {

            // Clear all previous errors
            if (field == null) {
                nameInput.setError(null);
                locationInput.setError(null);
                dateInput.setError(null);
                timeInput.setError(null);
                capacityInput.setError(null);
                genreText.setError(null);
                descriptionInput.setError(null);
                return;
            }

            // Show validation error according to the failing field
            switch (field) {
                case TITLE:
                    nameInput.setError(getString(R.string.error_event_title));
                    nameInput.requestFocus();
                    break;

                case LOCATION:
                    locationInput.setError(getString(R.string.error_event_location));
                    locationInput.requestFocus();
                    break;

                case DATE:
                    dateInput.setError(getString(R.string.error_event_date));
                    dateInput.requestFocus();
                    break;

                case TIME:
                    timeInput.setError(getString(R.string.error_event_time));
                    timeInput.requestFocus();
                    break;

                case GENRE:
                    genreText.setError(getString(R.string.error_event_genre));
                    genreText.requestFocus();
                    break;

                case CAPACITY:
                    capacityInput.setError(getString(R.string.error_event_capacity));
                    capacityInput.requestFocus();
                    break;

                case DESCRIPTION:
                    descriptionInput.setError(getString(R.string.error_event_description));
                    descriptionInput.requestFocus();
                    break;
            }
        });

        // Update selected genres text
        viewModel.getGenresText().observe(this, text -> {
            genreText.setText(text);
            if (text != null && !text.trim().isEmpty()) {
                genreText.setError(null);
            }
        });

        // Update date text
        viewModel.getDateText().observe(this, text -> {
            dateInput.setText(text);
            dateInput.setError(null);
        });

        // Update time text
        viewModel.getTimeText().observe(this, text -> {
            timeInput.setText(text);
            timeInput.setError(null);
        });

        // Update location text
        viewModel.getLocationText().observe(this, text -> {
            locationInput.setText(text);
            locationInput.setError(null);
        });

        // Show toast messages from ViewModel
        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Finish screen on successful creation
        viewModel.getSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, getString(R.string.event_created_success), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Sets up all UI event listeners and delegates actions to the ViewModel.
     */
    private void setupListeners() {

        // Open map picker for selecting a location
        mapButton.setOnClickListener(v ->
                mapPickerLauncher.launch(
                        new Intent(this, MapPickerActivity.class)));

        locationInput.setOnClickListener(v ->
                mapPickerLauncher.launch(
                        new Intent(this, MapPickerActivity.class)));

        // Show date picker dialog
        dateInput.setOnClickListener(v ->
                new DatePickerDialog(
                        this,
                        (view, y, m, d) -> viewModel.setDate(y, m, d),
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        // Show time picker dialog
        timeInput.setOnClickListener(v ->
                new TimePickerDialog(
                        this,
                        (view, h, m) -> viewModel.setTime(h, m),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                ).show()
        );

        // Open genre selection dialog
        genreText.setOnClickListener(v -> openGenreDialog());

        // Clear errors when user edits fields
        nameInput.addTextChangedListener(new SimpleTextWatcher(() ->
                nameInput.setError(null)
        ));
        capacityInput.addTextChangedListener(new SimpleTextWatcher(() ->
                capacityInput.setError(null)
        ));
        descriptionInput.addTextChangedListener(
                new SimpleTextWatcher(() ->
                        descriptionInput.setError(null)
                )
        );

        // Publish event
        publishBtn.setOnClickListener(v ->
                viewModel.publish(
                        nameInput.getText().toString(),
                        capacityInput.getText().toString(),
                        descriptionInput.getText().toString()
                ));

        // Cancel creation
        cancelBtn.setOnClickListener(v -> finish());
    }

    /**
     * Opens a multi-choice dialog that allows selecting one or more music genres.
     * The selected values are stored and managed by the ViewModel.
     */
    private void openGenreDialog() {
        MusicGenre[] allGenres = MusicGenre.values();
        String[] labels = new String[allGenres.length];
        for (int i = 0; i < allGenres.length; i++) {
            labels[i] = allGenres[i].getDisplayName();
        }

        boolean[] checked = viewModel.getCheckedGenres(allGenres);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_music_genres))
                .setMultiChoiceItems(
                        labels,
                        checked,
                        (dialog, which, isChecked) ->
                                viewModel.toggleGenre(
                                        allGenres[which],
                                        isChecked
                                )
                )
                .setPositiveButton(getString(R.string.ok), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Simple TextWatcher that executes a callback whenever the text changes.
     * Used here to clear validation errors while the user edits input.
     */
    private static class SimpleTextWatcher implements android.text.TextWatcher {

        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    /**
     * Handles selection from the navigation menu.
     */
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    /**
     * Replaces the ViewModel instance for testing purposes.
     */
    public void setTestingViewModel(CreateNewEventViewModel testViewModel) {
        this.viewModel = testViewModel;
        observeViewModel();
    }

    /**
     * Returns the current ViewModel instance (mainly for testing).
     */
    public CreateNewEventViewModel getViewModel() { return viewModel; }

}
