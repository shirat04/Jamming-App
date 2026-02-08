package com.example.jamming.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.jamming.R;
import com.example.jamming.model.User;
import com.example.jamming.view.navigation.OwnerMenuHandler;
import com.example.jamming.viewmodel.OwnerProfileViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying and managing the owner's profile.
 *
 * Features:
 * - View profile info (name, email, profile image)
 * - Change profile picture (camera or gallery)
 * - Edit profile (navigates to UpdateOwnerProfileActivity)
 * - Change password (sends reset email)
 * - Toggle notifications
 * - Logout
 * - Delete account
 *
 * Follows MVVM architecture - all business logic is in OwnerProfileViewModel.
 */
public class OwnerProfileActivity extends BaseActivity {

    // ===================== ViewModel =====================
    private OwnerProfileViewModel viewModel;

    // ===================== Menu Handler =====================
    private OwnerMenuHandler menuHandler;

    // ===================== Views =====================
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private LinearLayout rowEditProfile;
    private LinearLayout rowChangePassword;
    private SwitchCompat notificationSwitch;
    private Button btnLogout;
    private Button btnDeleteAccount;
    private ProgressBar progressBar;
    private View uploadProgressOverlay;
    private ProgressBar uploadProgressBar;
    private TextView uploadProgressText;

    // ===================== Camera/Gallery Handling =====================
    private Uri cameraImageUri;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> galleryPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup base activity with menu and layout
        setupBase(R.menu.owner_menu, R.layout.activity_owner_profile);
        setTitleText("My Profile");

        // Initialize menu handler
        menuHandler = new OwnerMenuHandler(this);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(OwnerProfileViewModel.class);

        // Initialize views
        initViews();

        // Setup activity result launchers
        setupActivityResultLaunchers();

        // Setup click listeners
        setupClickListeners();

        // Observe ViewModel
        observeViewModel();

        // Load owner profile
        viewModel.loadOwnerProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile when returning from edit screen
        viewModel.loadOwnerProfile();
    }

    @Override
    protected boolean onMenuItemSelected(int itemId) {
        return menuHandler.handle(itemId);
    }

    // ===================== Initialization =====================

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        rowEditProfile = findViewById(R.id.rowEditProfile);
        rowChangePassword = findViewById(R.id.rowChangePassword);
        notificationSwitch = findViewById(R.id.switchNotifications);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Progress indicators
        progressBar = findViewById(R.id.progressBar);
        uploadProgressOverlay = findViewById(R.id.uploadProgressOverlay);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        uploadProgressText = findViewById(R.id.uploadProgressText);
    }

    private void setupActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            viewModel.uploadProfileImage(selectedImage);
                        }
                    }
                }
        );

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                        viewModel.uploadProfileImage(cameraImageUri);
                    }
                }
        );

        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Gallery permission launcher (for older Android versions)
        galleryPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is required",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        // Profile image click - show image picker dialog
        profileImage.setOnClickListener(v -> showImagePickerDialog());

        // Edit profile
        rowEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateOwnerProfileActivity.class);
            startActivity(intent);
        });

        // Change password
        rowChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Notification switch
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only update if user initiated the change (not programmatic)
            if (buttonView.isPressed()) {
                viewModel.updateNotificationsEnabled(isChecked);
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Delete account
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    // ===================== ViewModel Observation =====================

    private void observeViewModel() {
        // Observe owner data
        viewModel.getOwner().observe(this, this::updateUI);

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe upload progress
        viewModel.getUploadProgress().observe(this, progress -> {
            if (progress != null && progress > 0 && progress < 100) {
                showUploadProgress(progress);
            } else {
                hideUploadProgress();
            }
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

        // Observe logout success
        viewModel.getLogoutSuccess().observe(this, success -> {
            if (success != null && success) {
                navigateToLogin();
            }
        });

        // Observe delete success
        viewModel.getDeleteSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Account deleted successfully",
                        Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        });

        // Observe password reset sent
        viewModel.getPasswordResetSent().observe(this, sent -> {
            if (sent != null && sent) {
                new AlertDialog.Builder(this)
                        .setTitle("Check Your Email")
                        .setMessage("A password reset link has been sent to your email address.")
                        .setPositiveButton("OK", null)
                        .show();
                viewModel.resetPasswordResetSent();
            }
        });
    }

    // ===================== UI Updates =====================

    private void updateUI(User owner) {
        if (owner == null) return;

        // Update name
        String displayName = owner.getFullName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = owner.getUsername();
        }
        profileName.setText(displayName != null ? displayName : "Owner");

        // Update email
        profileEmail.setText(owner.getEmail() != null ? owner.getEmail() : "");

        // Update profile image
        if (owner.getProfileImageUrl() != null && !owner.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(owner.getProfileImageUrl())
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_profile_image);
        }

        // Update notification switch (without triggering listener)
        notificationSwitch.setOnCheckedChangeListener(null);
        notificationSwitch.setChecked(owner.isNotificationsEnabled());
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                viewModel.updateNotificationsEnabled(isChecked);
            }
        });
    }

    private void showUploadProgress(int progress) {
        if (uploadProgressOverlay != null) {
            uploadProgressOverlay.setVisibility(View.VISIBLE);
        }
        if (uploadProgressBar != null) {
            uploadProgressBar.setProgress(progress);
        }
        if (uploadProgressText != null) {
            uploadProgressText.setText(progress + "%");
        }
    }

    private void hideUploadProgress() {
        if (uploadProgressOverlay != null) {
            uploadProgressOverlay.setVisibility(View.GONE);
        }
    }

    // ===================== Dialogs =====================

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Photo
                            checkCameraPermissionAndOpen();
                            break;
                        case 1: // Choose from Gallery
                            checkGalleryPermissionAndOpen();
                            break;
                        case 2: // Cancel
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void showChangePasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setMessage("We will send a password reset link to your email address. Continue?")
                .setPositiveButton("Send", (dialog, which) -> {
                    viewModel.sendPasswordResetEmail();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    viewModel.logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? " +
                        "This action cannot be undone and all your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show second confirmation
                    showFinalDeleteConfirmation();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFinalDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This is your last chance! Are you absolutely sure?")
                .setPositiveButton("Yes, Delete My Account", (dialog, which) -> {
                    viewModel.deleteAccount();
                })
                .setNegativeButton("No, Keep My Account", null)
                .show();
    }

    // ===================== Camera/Gallery Handling =====================

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkGalleryPermissionAndOpen() {
        // For Android 13+ (API 33+), we don't need READ_EXTERNAL_STORAGE for picking images
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 - scoped storage, no permission needed for picker
            openGallery();
        } else {
            // Android 9 and below
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there's a camera app available
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this,
                    "No camera available. Please use Gallery instead.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // ===================== Navigation =====================

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}