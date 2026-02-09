package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.User;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel responsible for managing the Update Profile screen.
 *
 * This ViewModel:
 * - Loads current user profile data
 * - Validates profile updates
 * - Handles profile field updates (fullName, phone, username)
 * - Exposes UI state via LiveData
 *
 * Follows the MVVM pattern:
 * - No direct UI references
 * - Exposes state via LiveData
 * - Delegates data access to repositories
 */
public class UpdateProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    /**
     * Default constructor used in production.
     */
    public UpdateProfileViewModel() {
        this(new AuthRepository(), new UserRepository());
    }

    /**
     * Constructor for dependency injection (mainly for testing).
     */
    public UpdateProfileViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    // ===================== LiveData =====================

    /** Current user profile data */
    private final MutableLiveData<User> user = new MutableLiveData<>();

    /** Loading state indicator */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Error message to display */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /** Success message to display */
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    /** Update success indicator - triggers navigation back */
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);

    // ===================== Getters for LiveData =====================

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    // ===================== Public Methods =====================

    /**
     * Loads the current user's profile from Firestore.
     */
    public void loadUserProfile() {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);

        userRepository.getUserById(uid)
                .addOnSuccessListener(doc -> {
                    isLoading.setValue(false);

                    if (!doc.exists()) {
                        errorMessage.setValue("User profile not found");
                        return;
                    }

                    User userProfile = doc.toObject(User.class);
                    if (userProfile != null) {
                        userProfile.setFirebaseId(uid);
                        user.setValue(userProfile);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to load profile: " + e.getMessage());
                });
    }

    /**
     * Updates user profile with new information.
     * Validates input and updates only changed fields.
     *
     * @param fullName User's full name
     * @param phone User's phone number
     * @param username User's username
     */
    public void updateProfile(String fullName, String phone, String username) {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        // Trim inputs
        fullName = fullName != null ? fullName.trim() : "";
        phone = phone != null ? phone.trim() : "";
        username = username != null ? username.trim() : "";

        // Validation
        if (fullName.isEmpty()) {
            errorMessage.setValue("Full name is required");
            return;
        }

        if (username.isEmpty()) {
            errorMessage.setValue("Username is required");
            return;
        }

        // Validate username format (only letters, numbers, underscore)
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            errorMessage.setValue("Username must be 3-20 characters (letters, numbers, underscore only)");
            return;
        }

        // Validate phone format if provided
        if (!phone.isEmpty() && !isValidPhone(phone)) {
            errorMessage.setValue("Invalid phone number format");
            return;
        }

        // Check if username changed - if so, verify it's not taken
        User currentUser = user.getValue();
        if (currentUser != null && !username.equals(currentUser.getUsername())) {
            checkUsernameAndUpdate(uid, fullName, phone, username);
        } else {
            performUpdate(uid, fullName, phone, username);
        }
    }

    /**
     * Clears the error message.
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    /**
     * Clears the success message.
     */
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    /**
     * Resets the update success flag.
     */
    public void resetUpdateSuccess() {
        updateSuccess.setValue(false);
    }

    // ===================== Private Helper Methods =====================

    /**
     * Checks if username is available before updating.
     */
    private void checkUsernameAndUpdate(String uid, String fullName, String phone, String username) {
        isLoading.setValue(true);

        userRepository.getUserByUsername(username)
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Username is already taken");
                        return;
                    }

                    performUpdate(uid, fullName, phone, username);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to verify username: " + e.getMessage());
                });
    }

    /**
     * Performs the actual profile update in Firestore.
     */
    private void performUpdate(String uid, String fullName, String phone, String username) {
        isLoading.setValue(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("username", username);

        // Only add phone if not empty
        if (!phone.isEmpty()) {
            updates.put("phone", phone);
        }

        userRepository.updateUserProfile(uid, updates)
                .addOnSuccessListener(v -> {
                    isLoading.setValue(false);

                    // Update local user object
                    User currentUser = user.getValue();
                    if (currentUser != null) {
                        currentUser.setFullName(fullName);
                        currentUser.setPhone(phone);
                        currentUser.setUsername(username);
                        user.setValue(currentUser);
                    }

                    successMessage.setValue("Profile updated successfully");
                    updateSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update profile: " + e.getMessage());
                });
    }

    /**
     * Validates phone number format.
     * Accepts formats like: +972-50-1234567, 050-1234567, 0501234567
     */
    private boolean isValidPhone(String phone) {
        // Remove common separators
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");

        // Check if it matches common patterns
        // Starts with + followed by digits, or starts with 0 followed by digits
        // Length between 9-15 digits
        return cleanPhone.matches("^(\\+?\\d{9,15}|0\\d{8,14})$");
    }
}