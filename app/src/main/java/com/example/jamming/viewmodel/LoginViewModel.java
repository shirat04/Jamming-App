package com.example.jamming.viewmodel;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.R;
import com.example.jamming.model.UserType;
import com.example.jamming.repository.AuthRepository;

/**
 * ViewModel responsible for handling the login flow.
 *
 * This class validates user input, communicates with the AuthRepository,
 * and exposes login results, loading state, and messages via LiveData.
 *
 * Follows the MVVM pattern:
 * - Contains no direct references to UI components
 * - Exposes observable state for the View to react to
 */
public class LoginViewModel extends ViewModel {

    /** Repository responsible for authentication and user data access */
    private final AuthRepository repo;

    /**
     * Default constructor used in production.
     * Initializes the ViewModel with the real AuthRepository.
     */
    public LoginViewModel() {
        this(new AuthRepository());
    }

    /**
     * Constructor for dependency injection (mainly used for testing).
     *
     * @param repo AuthRepository instance
     */
    public LoginViewModel(AuthRepository repo) {
        this.repo = repo;
    }

    /** Message exposed to the UI (errors and informational messages) */
    private final MutableLiveData<Integer> messageResId = new MutableLiveData<>();

    /** User type emitted on successful login */
    private final MutableLiveData<UserType> userType = new MutableLiveData<>();

    /** Loading indicator state for showing/hiding progress UI */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Read-only accessors for the View */
    public LiveData<Integer> getMessageResId() { return messageResId; }
    public LiveData<UserType> getUserType() { return userType; }
    public LiveData<Boolean> getIsLoading() {return isLoading;}

    /** Stops the loading state */
    public void stopLoading() {isLoading.setValue(false);}

    /**
     * Performs user login using either email or username.
     *
     * Flow:
     * 1. Validate input
     * 2. Determine whether identifier is email or username
     * 3. Authenticate user
     * 4. Load user type from database
     *
     * @param identifier email address or username
     * @param password user password
     */
    public void login(String identifier, String password) {

        // Basic input validation
        if (identifier == null || identifier.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            messageResId.setValue(R.string.error_fill_all_fields);
            return;
        }
        identifier = identifier.trim();
        isLoading.setValue(true);

        // Case 1: Identifier is an email -> authenticate directly
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            repo.login(identifier, password)
                    .addOnSuccessListener(auth -> checkUserType(repo.getCurrentUid()))
                    .addOnFailureListener(e -> {
                        stopLoading();
                        messageResId.setValue(R.string.error_incorrect_email_or_password);
                    });
            return;
        }

        // Case 2: Identifier is a username -> resolve email first
        repo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        stopLoading();
                        messageResId.setValue(R.string.error_incorrect_email_or_password);
                        return;
                    }

                    // Extract user's email from database
                    String email = query.getDocuments().get(0).getString("email");
                    if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        stopLoading();
                        messageResId.setValue(R.string.error_invalid_user_email);
                        return;
                    }

                    // Authenticate using resolved email
                    repo.login(email.trim(), password)
                            .addOnSuccessListener(auth -> {
                                checkUserType(repo.getCurrentUid());
                            })
                            .addOnFailureListener(e -> {
                                stopLoading();
                                messageResId.setValue(R.string.error_incorrect_username_or_password);
                            });
                })
                .addOnFailureListener(e -> {
                    stopLoading();
                    messageResId.setValue(R.string.error_login_failed_try_again);
                });
    }

    /**
     * Loads the user's type (USER / OWNER) after successful authentication.
     *
     * @param uid authenticated user's unique ID
     */
    private void checkUserType(String uid) {
        if (uid == null) {
            stopLoading();
            messageResId.setValue(R.string.error_authentication);
            return;
        }
        repo.getUserUId(uid)
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        messageResId.setValue(R.string.error_user_data_not_found);
                        return;
                    }

                    String typeStr = doc.getString("userType");
                    if (typeStr == null) {
                        messageResId.setValue(R.string.error_invalid_user_type);
                        return;
                    }

                    try {
                        userType.setValue(UserType.valueOf(typeStr));
                    } catch (IllegalArgumentException e) {
                        messageResId.setValue(R.string.error_invalid_user_type);
                    }
                })
                .addOnFailureListener(e -> {
                    stopLoading();
                    messageResId.setValue(R.string.error_failed_to_load_user_data);
                });
    }

    /**
     * Sends a password reset email.
     *
     * Supports both email and username identifiers.
     *
     * @param identifier email address or username
     */
    public void resetPassword(String identifier) {

        if (identifier == null || identifier.trim().isEmpty()) {
            messageResId.setValue(R.string.error_enter_username_or_email);
            return;
        }

        identifier = identifier.trim();

        // Case 1: Identifier is an email
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            repo.sendPasswordResetEmail(identifier)
                    .addOnSuccessListener(v ->
                            messageResId.setValue(R.string.msg_password_reset_sent))
                    .addOnFailureListener(e ->
                            messageResId.setValue(R.string.error_password_reset_failed));
            return;
        }

        // Case 2: Identifier is a username -> resolve email first
        repo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        messageResId.setValue(R.string.error_username_not_exists);
                        return;
                    }

                    String email = query.getDocuments()
                            .get(0)
                            .getString("email");

                    if (email == null ||
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        messageResId.setValue(R.string.error_invalid_user_email);
                        return;
                    }

                    repo.sendPasswordResetEmail(email)
                            .addOnSuccessListener(v ->
                                    messageResId.setValue(R.string.msg_password_reset_sent))
                            .addOnFailureListener(e ->
                                    messageResId.setValue(R.string.error_password_reset_failed));
                })
                .addOnFailureListener(e ->
                        messageResId.setValue(R.string.error_failed_to_retrieve_user_info));
    }

    /**
     * Clears the currently displayed message.
     * Used by the View when user starts editing input fields.
     */
    public void clearMessage() {
        messageResId.setValue(null);

    }


}