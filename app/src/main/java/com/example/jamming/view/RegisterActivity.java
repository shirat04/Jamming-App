package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import com.example.jamming.model.UserType;
import com.example.jamming.utils.FormTextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.viewmodel.RegisterViewModel;

/**
 * Activity responsible for rendering the registration screen and handling user interactions.
 *
 * This class follows the MVVM pattern:
 * - It contains no business logic or validation rules.
 * - It delegates all registration logic to {@link RegisterViewModel}.
 * - It observes LiveData from the ViewModel to update the UI and perform navigation.
 */
public class RegisterActivity extends AppCompatActivity {
    // Input fields
    private EditText fullName, password, confirmPassword, email, userName;

    // UI controls
    private RadioButton owner;
    private TextView errorText;
    private ProgressBar progressBar;
    private Button register,backLogin;

    // ViewModel handling the registration logic
    private RegisterViewModel viewModel;

    /**
     * Initializes the Activity, binds the UI elements, sets up listeners,
     * and starts observing the ViewModel state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind all views from the layout
        initViews();

        // Obtain the ViewModel instance scoped to this Activity
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Set up UI event listeners (buttons, etc.)
        setupListeners();

        // Clear error messages when the user edits any input field
        setupClearErrorOnInput(fullName, userName, email, password, confirmPassword);

        // Clear error message when the user changes the selected user type
        owner.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.clearError());

        // Observe ViewModel state and react to changes
        observeViewModel();
    }

    /**
     * Finds and assigns all required view references from the layout.
     * This method centralizes view binding and keeps onCreate() clean and readable.
     */
    private void initViews() {
        fullName = findViewById(R.id.etName);
        password = findViewById(R.id.etPassword);
        confirmPassword = findViewById(R.id.etConfirmPassword);
        email = findViewById(R.id.etEmail);
        owner = findViewById(R.id.rbOwner);
        userName = findViewById(R.id.usName);
        errorText = findViewById(R.id.errorText);
        progressBar = findViewById(R.id.registerProgressBar);
        register = findViewById(R.id.btnRegister);
        backLogin = findViewById(R.id.btnAlreadyHaveAccount);
    }

    /**
     * Sets up click listeners for the main UI actions:
     * - Navigating back to the login screen
     * - Submitting the registration form
     */
    private void setupListeners() {
        // Navigate back to the login screen
        backLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
        // Trigger the registration flow via the ViewModel
        register.setOnClickListener(v -> handleRegister());
    }

    /**
     * Collects raw input values from the UI and delegates the processing
     * to the ViewModel. The ViewModel is responsible for normalization,
     * validation, and executing the registration logic.
     */
    private void handleRegister() {
        viewModel.registerFromForm(
                fullName.getText().toString(),
                email.getText().toString(),
                password.getText().toString(),
                confirmPassword.getText().toString(),
                userName.getText().toString(),
                owner.isChecked()
        );
    }

    /**
     * Observes LiveData exposed by the ViewModel and updates the UI accordingly:
     * - Shows or hides the loading indicator
     * - Displays validation or server errors
     * - Navigates to the appropriate screen after successful registration
     */
    private void observeViewModel() {
        // Observe loading state to update progress indicator and UI interactivity
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            register.setEnabled(!isLoading);
            backLogin.setEnabled(!isLoading);
            register.setAlpha(isLoading ? 0.6f : 1f);
        });

        // Observe error messages and display them to the user
        viewModel.getErrorResId().observe(this, errResId -> {
            if (errResId != null) {
                errorText.setText(getString(errResId));
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.GONE);
            }
        });

        // Observe successful registration result and navigate to the next screen
        viewModel.getUserType().observe(this, type -> {
            if (type == null) return;
            if (type == UserType.OWNER) {
                startActivity(new Intent(this, OwnerActivity.class));
            } else {
                startActivity(new Intent(this, ExploreEventsActivity.class));
            }
            viewModel.clearNavigation();
            finish();
        });
    }

    /**
     * Attaches a shared TextWatcher to the given input fields.
     * Whenever the user edits any field, the current error message is cleared
     * to provide immediate feedback and avoid showing outdated errors.
     *
     * @param fields variable number of EditText fields to observe
     */
    private void setupClearErrorOnInput(EditText... fields) {
        for (EditText field : fields) {
            field.addTextChangedListener(
                    FormTextWatcher.after(text -> viewModel.clearError())
            );
        }
    }


}