package com.example.jamming.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.viewmodel.CreateNewEventViewModel;
import com.example.jamming.view.dialog.DatePickerDialogFragment;
import com.example.jamming.view.dialog.TimePickerDialogFragment;


public class CreateNewEventActivity extends AppCompatActivity {

    private EditText nameInput, capacityInput, descriptionInput, locationInput;
    private TextView dateInput, timeInput, genreText, genreErrorText, cancelBtn;
    private Button publishBtn;
    private LinearLayout genreContainer;

    private ImageButton mapButton;

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
        setContentView(R.layout.activity_create_new_event);

        viewModel = new ViewModelProvider(this)
                .get(CreateNewEventViewModel.class);

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
        genreErrorText = findViewById(R.id.genreErrorText);
        genreContainer = findViewById(R.id.genreContainer);
    }

    private void observeViewModel() {
        // ===== תאריך =====
        viewModel.getDateText().observe(this, text -> {
            if (text != null) {
                dateInput.setText(text);
                dateInput.setError(null);
            }
        });

        viewModel.getDateError().observe(this, msg -> {
            if (msg != null) {
                dateInput.setError(msg);
            }
        });

        // ===== שעה =====
        viewModel.getTimeText().observe(this, text -> {
            if (text != null) {
                timeInput.setText(text);
                timeInput.setError(null);
            }
        });

        viewModel.getTimeError().observe(this, msg -> {
            if (msg != null) {
                timeInput.setError(msg);
            }
        });

        // ===== מיקום =====
        viewModel.getLocationText().observe(this, text -> {
            if (text != null) {
                locationInput.setText(text);
                locationInput.setError(null);
            }
        });

        viewModel.getLocationError().observe(this, msg -> {
            if (msg != null) {
                locationInput.setError(msg);
            }
        });

        // ===== שם אירוע =====
        viewModel.getNameError().observe(this, msg -> {
            if (msg != null) {
                nameInput.setError(msg);
            }
        });

        // ===== קיבולת =====
        viewModel.getCapacityError().observe(this, msg -> {
            if (msg != null) {
                capacityInput.setError(msg);
            }
        });

        // ===== ז'אנר (TextView – בלי בועה) =====
        viewModel.getGenreError().observe(this, hasError -> {
            if (Boolean.TRUE.equals(hasError)) {
                genreContainer.setBackgroundResource(R.drawable.genre_error_bg);
                genreText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                genreErrorText.setVisibility(View.VISIBLE);
            } else {
                genreContainer.setBackgroundResource(R.drawable.genre_normal_bg);
                genreText.setTextColor(getResources().getColor(android.R.color.black));
                genreErrorText.setVisibility(View.GONE);
            }
        });

        // ===== הודעות כלליות =====
        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // ===== הצלחה =====
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

        dateInput.setOnClickListener(v -> {
            DatePickerDialogFragment dialog =
                    DatePickerDialogFragment.newInstance(
                            (year, month, day) ->
                                    viewModel.setDate(year, month, day));
            dialog.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        timeInput.setOnClickListener(v -> {
            TimePickerDialogFragment dialog =
                    TimePickerDialogFragment.newInstance(
                            (hour, minute) ->
                                    viewModel.setTime(hour, minute));
            dialog.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        genreText.setOnClickListener(v -> openGenreDialog());

        nameInput.addTextChangedListener(new SimpleTextWatcher(() ->
                nameInput.setError(null)
        ));

        capacityInput.addTextChangedListener(new SimpleTextWatcher(() ->
                capacityInput.setError(null)
        ));
        genreText.setOnClickListener(v -> {
            genreContainer.setBackgroundResource(
                    R.drawable.genre_normal_bg
            );
            openGenreDialog();
        });


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
                .setPositiveButton("OK", (d, w) -> {
                    genreText.setText(
                            viewModel.getGenresText()
                    );
                })
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


}
