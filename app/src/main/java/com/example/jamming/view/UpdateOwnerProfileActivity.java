package com.example.jamming.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.model.User;
import com.example.jamming.utils.FormTextWatcher;
import com.example.jamming.view.navigation.OwnerMenuHandler;
import com.example.jamming.viewmodel.UpdateProfileViewModel;

/**
 * Activity for updating owner profile information.
 *
 * Features:
 * - Edit business/owner name
 * - Edit phone number
 * - Edit username
 * - Form validation
 * - Save updates to Firestore
 *
 * Follows MVVM architecture - all business logic is in UpdateProfileViewModel.
 */
public class UpdateOwnerProfileActivity extends BaseActivity {

    // ===================== ViewModel =====================
    private UpdateProfileViewModel viewModel;

    // ===================== Menu Handler =====================
    private OwnerMenuHandler menuHandler;

    // ===================== Views =====================
    private EditText inputName;
    private EditText inputPhone;
    private EditText inputUsername;
    private EditText inputEmail; // Read-only, for display
    private Button btnSaveChanges;
    private Button btnCancel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup base activity with menu and layout
        setupBase(R.menu.owner_menu, R.layout.activity_update_owner_profile);
        setTitleText("Edit Profile");

        // Initialize menu handler
        menuHandler = new OwnerMenuHandler(this);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UpdateProfileViewModel.class);

        // Bind views
        initViews();

        // Setup listeners
        setupListeners();

        // Setup form text watchers to clear errors on input
        setupClearErrorOnInput();

        // Observe ViewModel
        observeViewModel();

        // Load current owner profile
        viewModel.loadUserProfile();
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    // ===================== View Initialization =====================

    private void initViews() {
        inputName = findViewById(R.id.inputName);
        inputPhone = findViewById(R.id.inputPhone);
        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.updateProgressBar);
    }

    // ===================== Listeners =====================

    private void setupListeners() {
        // Save changes button
        btnSaveChanges.setOnClickListener(v -> handleSaveChanges());

        // Cancel button - go back without saving
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Clears error messages when user starts typing in any field.
     */
    private void setupClearErrorOnInput() {
        inputName.addTextChangedListener(
                FormTextWatcher.after(text -> viewModel.clearErrorMessage())
        );
        inputPhone.addTextChangedListener(
                FormTextWatcher.after(text -> viewModel.clearErrorMessage())
        );
        inputUsername.addTextChangedListener(
                FormTextWatcher.after(text -> viewModel.clearErrorMessage())
        );
    }

    // ===================== ViewModel Observers =====================

    private void observeViewModel() {
        // Observe owner profile data
        viewModel.getUser().observe(this, owner -> {
            if (owner != null) {
                populateForm(owner);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSaveChanges.setEnabled(!isLoading);
            btnCancel.setEnabled(!isLoading);

            // Disable input fields during loading
            inputName.setEnabled(!isLoading);
            inputPhone.setEnabled(!isLoading);
            inputUsername.setEnabled(!isLoading);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });

        // Observe update success - navigate back on success
        viewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                viewModel.resetUpdateSuccess();
                // Delay finish to allow toast to show
                btnSaveChanges.postDelayed(this::finish, 500);
            }
        });
    }

    // ===================== UI Updates =====================

    /**
     * Populates form fields with owner data.
     */
    private void populateForm(User owner) {
        if (owner == null) return;

        inputName.setText(owner.getFullName() != null ? owner.getFullName() : "");
        inputPhone.setText(owner.getPhone() != null ? owner.getPhone() : "");
        inputUsername.setText(owner.getUsername() != null ? owner.getUsername() : "");
        inputEmail.setText(owner.getEmail() != null ? owner.getEmail() : "");
    }

    /**
     * Handles save changes button click.
     */
    private void handleSaveChanges() {
        String name = inputName.getText().toString();
        String phone = inputPhone.getText().toString();
        String username = inputUsername.getText().toString();

        viewModel.updateProfile(name, phone, username);
    }
}