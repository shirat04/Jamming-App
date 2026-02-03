package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.model.UserType;
import com.example.jamming.repository.AuthRepository;
import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel responsible for handling the user registration flow.
 *
 * Responsibilities:
 * - Validate user input (required fields, email format, password rules)
 * - Check username availability
 * - Create an authentication user via Firebase
 * - Persist user profile data in Firestore
 * - Expose loading, error and navigation state via LiveData
 *
 * This ViewModel follows the MVVM pattern:
 * - Contains no direct references to UI components
 * - Exposes state changes through LiveData only
 */
public class RegisterViewModel extends ViewModel {

    /** Repository handling authentication and user data persistence */
    private final AuthRepository repo;

    /**
     * Default constructor used in production.
     * Initializes the ViewModel with the real AuthRepository.
     */
    public RegisterViewModel() {
        this(new AuthRepository());
    }

    /**
     * Constructor for dependency injection (mainly for testing).
     *
     * @param repo AuthRepository instance
     */
    public RegisterViewModel(AuthRepository repo) {
        this.repo = repo;
    }

    /** Error message exposed to the UI */
    private final MutableLiveData<String> error = new MutableLiveData<>();

    /**
     * Selected user type emitted on successful registration.
     * Used by the View to navigate to the correct next screen.
     */
    private final MutableLiveData<UserType> userType = new MutableLiveData<>();

    /**
     * Loading indicator state.
     * True while registration is in progress, false otherwise.
     */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Read-only accessors for UI observers */
    public LiveData<Boolean> getIsLoading() {return isLoading;}

    public LiveData<String> getError() { return error; }
    public LiveData<UserType> getUserType() { return userType; }


    /**
     * Performs the registration flow.
     *
     * Flow:
     * 1. Clear previous errors and start loading
     * 2. Validate required fields
     * 3. Validate email format and password rules
     * 4. Check if the chosen username is already taken
     * 5. Create authentication user (Firebase Auth)
     * 6. Save user profile data to Firestore
     *
     * Loading remains active until either:
     * - An error occurs, or
     * - Registration succeeds and navigation is triggered
     *
     * @param fullName user's full name
     * @param email user's email address
     * @param pass chosen password
     * @param confPass password confirmation
     * @param userName chosen username
     * @param type selected user type (ENUM)
     */
    public void register(String fullName, String email, String pass,String confPass, String userName, UserType type) {
        // Reset previous error and start loading
        error.setValue(null);
        isLoading.setValue(true);

        // Basic required-fields validation
        if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                pass == null || pass.isEmpty() ||
                userName == null || userName.trim().isEmpty() ||
                type == null) {
            isLoading.setValue(false);
            error.setValue("Please fill in all fields.");
            return;
        }
        // Check if username is already taken
        repo.isUsernameTaken(userName)
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        isLoading.setValue(false);
                        error.setValue("Username is taken. Try another one.");
                        return;
                    }

        // Email format validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isLoading.setValue(false);
            error.setValue("Please enter a valid email address.");
            return;
        }
        if(pass.length()<6){
            isLoading.setValue(false);
            error.setValue("The password must be at least six characters long.");
            return;
        }

        // Password validation
        if (!pass.equals(confPass)) {
            isLoading.setValue(false);
            error.setValue("The passwords do not match.");
            return;
        }
                    // Create authentication user
                    repo.createUser(email, pass)
                            .addOnSuccessListener(auth -> {
                                String uid = repo.getCurrentUid();
                                saveUserProfile(uid, fullName, email, userName, type);
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                error.setValue(e.getMessage());
                            });                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    error.setValue(e.getMessage());
                });
    }

    /**
     * Saves the user's profile data in Firestore after successful authentication.
     *
     * On success:
     * - Emits the userType LiveData
     * - Navigation is handled by the observing View
     *
     * Loading is stopped only on failure.
     *
     * @param uid authenticated user's unique ID
     * @param fullName user's full name
     * @param email user's email
     * @param userName chosen username
     * @param type selected user type (ENUM)
     */
    private void saveUserProfile(String uid, String fullName, String email, String userName, UserType type) {

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("email", email);
        data.put("username", userName);
        data.put("userType", type.name());

        repo.saveUserProfile(uid, data)
                .addOnSuccessListener(a -> {
                    userType.setValue(type);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    error.setValue(e.getMessage());
                });

    }

    /**
     * Clears the current error message.
     * Called when the user starts editing input fields.
     */
    public void clearError() {
        error.setValue(null);
    }


}
