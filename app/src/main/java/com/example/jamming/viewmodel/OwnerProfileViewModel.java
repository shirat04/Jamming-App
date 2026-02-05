package com.example.jamming.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.User;
import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.UserRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * ViewModel responsible for managing the Owner Profile screen.
 *
 * This ViewModel:
 * - Loads owner profile data
 * - Handles profile image upload (camera/gallery)
 * - Manages notification settings
 * - Handles logout and account deletion
 * - Handles password reset
 *
 * Follows the MVVM pattern:
 * - No direct UI references
 * - Exposes state via LiveData
 * - Delegates data access to repositories
 */
public class OwnerProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final StorageReference storageRef;

    /**
     * Default constructor used in production.
     */
    public OwnerProfileViewModel() {
        this(new AuthRepository(), new UserRepository(),
                FirebaseStorage.getInstance().getReference());
    }

    /**
     * Constructor for dependency injection (mainly for testing).
     */
    public OwnerProfileViewModel(
            AuthRepository authRepository,
            UserRepository userRepository,
            StorageReference storageRef
    ) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.storageRef = storageRef;
    }

    // ===================== LiveData Fields =====================

    /** Owner profile data */
    private final MutableLiveData<User> owner = new MutableLiveData<>();

    /** Loading state indicator */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Error messages to display */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /** Success messages (Toast) */
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    /** Indicates if logout was successful */
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    /** Indicates if account deletion was successful */
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    /** Indicates if password reset email was sent */
    private final MutableLiveData<Boolean> passwordResetSent = new MutableLiveData<>();

    /** Profile image upload progress (0-100) */
    private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>();

    // ===================== LiveData Getters =====================

    public LiveData<User> getOwner() { return owner; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }
    public LiveData<Boolean> getPasswordResetSent() { return passwordResetSent; }
    public LiveData<Integer> getUploadProgress() { return uploadProgress; }

    // ===================== Public Methods =====================

    /**
     * Loads the current owner's profile data.
     * Called when the activity starts.
     */
    public void loadOwnerProfile() {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("Owner not logged in");
            return;
        }

        isLoading.setValue(true);

        userRepository.getUserById(uid)
                .addOnSuccessListener(doc -> {
                    isLoading.setValue(false);

                    if (!doc.exists()) {
                        errorMessage.setValue("Owner profile not found");
                        return;
                    }

                    User ownerProfile = doc.toObject(User.class);
                    if (ownerProfile != null) {
                        ownerProfile.setFirebaseId(uid);
                        owner.setValue(ownerProfile);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to load profile: " + e.getMessage());
                });
    }

    /**
     * Uploads a profile image to Firebase Storage and updates the owner profile.
     *
     * @param imageUri URI of the selected image (from camera or gallery)
     */
    public void uploadProfileImage(Uri imageUri) {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("Owner not logged in");
            return;
        }

        if (imageUri == null) {
            errorMessage.setValue("No image selected");
            return;
        }

        isLoading.setValue(true);
        uploadProgress.setValue(0);

        // Create unique filename for the image
        String fileName = "profile_images/" + uid + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    // Calculate and emit upload progress
                    double progress = (100.0 * snapshot.getBytesTransferred())
                            / snapshot.getTotalByteCount();
                    uploadProgress.setValue((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after successful upload
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                updateProfileImageUrl(uid, downloadUri.toString());
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                errorMessage.setValue("Failed to get image URL");
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    uploadProgress.setValue(0);
                    errorMessage.setValue("Failed to upload image: " + e.getMessage());
                });
    }

    /**
     * Updates the notifications enabled setting for the owner.
     *
     * @param enabled Whether notifications should be enabled
     */
    public void updateNotificationsEnabled(boolean enabled) {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("Owner not logged in");
            return;
        }

        userRepository.updateNotificationsEnabled(uid, enabled)
                .addOnSuccessListener(v -> {
                    // Update local owner object
                    User currentOwner = owner.getValue();
                    if (currentOwner != null) {
                        currentOwner.setNotificationsEnabled(enabled);
                        owner.setValue(currentOwner);
                    }
                    successMessage.setValue(enabled
                            ? "Notifications enabled"
                            : "Notifications disabled");
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to update notification settings");
                });
    }

    /**
     * Sends a password reset email to the owner's email address.
     */
    public void sendPasswordResetEmail() {
        User currentOwner = owner.getValue();

        if (currentOwner == null || currentOwner.getEmail() == null) {
            errorMessage.setValue("Email not available");
            return;
        }

        isLoading.setValue(true);

        authRepository.sendPasswordResetEmail(currentOwner.getEmail())
                .addOnSuccessListener(v -> {
                    isLoading.setValue(false);
                    passwordResetSent.setValue(true);
                    successMessage.setValue("Password reset email sent");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to send reset email: " + e.getMessage());
                });
    }

    /**
     * Logs out the current owner.
     */
    public void logout() {
        authRepository.logout();
        logoutSuccess.setValue(true);
    }

    /**
     * Deletes the owner's account completely.
     * This includes:
     * 1. Deleting owner profile from Firestore
     * 2. Deleting profile image from Storage (if exists)
     * 3. Deleting Firebase Auth account
     */
    public void deleteAccount() {
        String uid = authRepository.getCurrentUid();

        if (uid == null) {
            errorMessage.setValue("Owner not logged in");
            return;
        }

        isLoading.setValue(true);

        // First, delete profile image if exists
        User currentOwner = owner.getValue();
        if (currentOwner != null && currentOwner.getProfileImageUrl() != null) {
            deleteProfileImageFromStorage(currentOwner.getProfileImageUrl());
        }

        // Delete owner profile from Firestore
        userRepository.deleteUserProfile(uid)
                .addOnSuccessListener(v -> {
                    // Delete Firebase Auth account
                    deleteFirebaseAuthAccount();
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to delete account: " + e.getMessage());
                });
    }

    /**
     * Returns the current owner's UID.
     */
    public String getCurrentUid() {
        return authRepository.getCurrentUid();
    }

    /**
     * Clears the error message after it was displayed.
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    /**
     * Clears the success message after it was displayed.
     */
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    /**
     * Resets the password reset sent flag.
     */
    public void resetPasswordResetSent() {
        passwordResetSent.setValue(false);
    }

    // ===================== Private Helper Methods =====================

    /**
     * Updates the profile image URL in Firestore after successful upload.
     */
    private void updateProfileImageUrl(String uid, String imageUrl) {
        userRepository.updateProfileImage(uid, imageUrl)
                .addOnSuccessListener(v -> {
                    isLoading.setValue(false);
                    uploadProgress.setValue(100);

                    // Update local owner object
                    User currentOwner = owner.getValue();
                    if (currentOwner != null) {
                        currentOwner.setProfileImageUrl(imageUrl);
                        owner.setValue(currentOwner);
                    }

                    successMessage.setValue("Profile image updated");
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to update profile image URL");
                });
    }

    /**
     * Deletes the profile image from Firebase Storage.
     */
    private void deleteProfileImageFromStorage(String imageUrl) {
        try {
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnFailureListener(e -> {
                        // Log error but continue with account deletion
                    });
        } catch (Exception e) {
            // URL might be invalid, continue with account deletion
        }
    }

    /**
     * Deletes the Firebase Authentication account.
     */
    private void deleteFirebaseAuthAccount() {
        authRepository.deleteCurrentUser()
                .addOnSuccessListener(v -> {
                    isLoading.setValue(false);
                    deleteSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to delete auth account: " + e.getMessage());
                });
    }
}