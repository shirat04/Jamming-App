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

import com.example.jamming.viewmodel.CreateNewEventViewModel;

import java.util.Calendar;


public class CreateNewEventActivity extends BaseActivity {

    private EditText nameInput, capacityInput, descriptionInput, locationInput;
    private TextView dateInput, timeInput, genreText, cancelBtn;
    private Button publishBtn;
    private ImageButton mapButton;
    private Calendar calendar;
    private OwnerMenuHandler menuHandler;



    private CreateNewEventViewModel viewModel;

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            viewModel.onLocationSelected(
                                    result.getData().getDoubleExtra("lat", 0),
                                    result.getData().getDoubleExtra("lng", 0),
                                    result.getData().getStringExtra("address")
                            );
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBase(
                R.menu.owner_menu,
                R.layout.activity_create_new_event
        );
        setTitleText(getString(R.string.create_new_event));
        hideRightActions();

        viewModel = new ViewModelProvider(this).get(CreateNewEventViewModel.class);
        calendar = viewModel.getDateTime();
        menuHandler = new OwnerMenuHandler(this);

        initViews();
        observeViewModel();
        setupListeners();
    }

    private void initViews() {

        nameInput = findViewById(R.id.eventNameInput);
        capacityInput = findViewById(R.id.eventCapacityInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        locationInput = findViewById(R.id.eventLocationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        genreText = findViewById(R.id.genreSpinner);
        publishBtn = findViewById(R.id.publishEventBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        mapButton = findViewById(R.id.mapButton);
    }

    private void observeViewModel() {
        viewModel.getErrorField().observe(this, field -> {

            if (field == null) {
                nameInput.setError(null);
                locationInput.setError(null);
                dateInput.setError(null);
                timeInput.setError(null);
                capacityInput.setError(null);
                genreText.setError(null);
                return;
            }

            switch (field) {
                case TITLE:
                    nameInput.setError("נא להזין שם אירוע");
                    nameInput.requestFocus();
                    break;

                case LOCATION:
                    locationInput.setError("נא לבחור מיקום");
                    locationInput.requestFocus();
                    break;

                case DATE:
                    dateInput.setError("נא לבחור תאריך");
                    dateInput.requestFocus();
                    break;

                case TIME:
                    timeInput.setError("נא לבחור שעה");
                    timeInput.requestFocus();
                    break;

                case GENRE:
                    genreText.setError("נא לבחור ז'אנר");
                    genreText.requestFocus();
                    break;

                case CAPACITY:
                    capacityInput.setError("קיבולת לא תקינה");
                    capacityInput.requestFocus();
                    break;

            }
        });

        viewModel.getGenresTextLive().observe(this, text -> {
            genreText.setText(text);
            if (text != null && !text.trim().isEmpty()) {
                genreText.setError(null);
            }
        });
        viewModel.getDateText().observe(this, text -> {
            dateInput.setText(text);
            dateInput.setError(null);
        });

        viewModel.getTimeText().observe(this, text -> {
            timeInput.setText(text);
            timeInput.setError(null);
        });

        viewModel.getLocationText().observe(this, text -> {
            locationInput.setText(text);
            locationInput.setError(null);
        });



        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "האירוע נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupListeners() {

        mapButton.setOnClickListener(v ->
                mapPickerLauncher.launch(
                        new Intent(this, MapPickerActivity.class)));

        locationInput.setOnClickListener(v ->
                mapPickerLauncher.launch(
                        new Intent(this, MapPickerActivity.class)));

        dateInput.setOnClickListener(v ->
                new DatePickerDialog(
                        this,
                        (view, y, m, d) -> viewModel.setDate(y, m, d),
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
        );


        timeInput.setOnClickListener(v ->
                new TimePickerDialog(
                        this,
                        (view, h, m) -> viewModel.setTime(h, m),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                ).show()
        );


        genreText.setOnClickListener(v -> openGenreDialog());

        nameInput.addTextChangedListener(new SimpleTextWatcher(() ->
                nameInput.setError(null)
        ));

        capacityInput.addTextChangedListener(new SimpleTextWatcher(() ->
                capacityInput.setError(null)
        ));
        genreText.setOnClickListener(v -> openGenreDialog());


        publishBtn.setOnClickListener(v ->
                viewModel.publish(
                        nameInput.getText().toString(),
                        capacityInput.getText().toString(),
                        descriptionInput.getText().toString()
                ));

        cancelBtn.setOnClickListener(v -> finish());
    }
    private void openGenreDialog() {

        String[] allGenres = getResources()
                .getStringArray(R.array.music_genres);

        boolean[] checked = viewModel.getCheckedGenres(allGenres);

        new AlertDialog.Builder(this)
                .setTitle("Select music genres")
                .setMultiChoiceItems(
                        allGenres,
                        checked,
                        (dialog, which, isChecked) ->
                                viewModel.toggleGenre(
                                        allGenres[which],
                                        isChecked
                                )
                )
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {

        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
            onChange.run();
        }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    public void setTestingViewModel(CreateNewEventViewModel testViewModel) {
        this.viewModel = testViewModel;
        observeViewModel();
    }
    public CreateNewEventViewModel getViewModel() { return viewModel; }

}
