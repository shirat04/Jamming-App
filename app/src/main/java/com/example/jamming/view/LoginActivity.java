package com.example.jamming.view;

import android.os.Bundle;
import android.content.Intent;
import com.example.jamming.utils.FormTextWatcher;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.viewmodel.LoginViewModel;
import com.example.jamming.model.UserType;

import com.google.android.gms.common.SignInButton;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;





/**
 * Activity responsible for handling the login screen UI.
 *
 * This class follows the MVVM pattern:
 * - Collects user input (username/email and password)
 * - Delegates all authentication logic to {@link LoginViewModel}
 * - Observes ViewModel LiveData to update the UI (loading, errors, navigation)
 * - Contains no business logic related to authentication
 */
public class LoginActivity extends AppCompatActivity {

    // Input fields
    private EditText usernameInput, passwordInput;

    // ViewModel that handles the login logic
    private LoginViewModel viewModel;

    // UI elements
    private ProgressBar progressBar;
    private Button loginBtn, registerBtn;
    private TextView errorText, forgotPassword;
    private SignInButton googleBtn;
    private GoogleSignInClient googleClient;

    private void setupGoogleClient() {
        String webClientId = getString(R.string.default_web_client_id);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) return;

                try {
                    GoogleSignInAccount acc =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                    .getResult(ApiException.class);

                    String idToken = acc.getIdToken();
                    if (idToken != null) {
                        viewModel.onGoogleIdTokenReceived(idToken);
                    }

                } catch (ApiException e) {
                    Log.e("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
                }
            });




    /**
     * Called when the Activity is created.
     * Initializes the UI, ViewModel, listeners and LiveData observers.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        setupGoogleClient();


        setupClearErrorOnInput();
        setupListeners();
        observeViewModel();

    }

    /**
     * Finds and caches all required view references from the layout.
     */
    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        registerBtn = findViewById(R.id.registerText);
        errorText = findViewById(R.id.errorTextView);
        forgotPassword = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.loginProgressBar);
        googleBtn = findViewById(R.id.googleSignInButton);
    }

    /**
     * Sets up all UI click listeners and delegates actions to the ViewModel.
     */
    private void setupListeners() {
        // Attempt login with the provided credentials
        loginBtn.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            viewModel.login(identifier, pass);
        });

        // Trigger password reset using the provided identifier
        forgotPassword.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            viewModel.resetPassword(identifier);
        });

        // Navigate to the registration screen
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        googleBtn.setOnClickListener(v -> {
            errorText.setText("Google button clicked");
            errorText.setVisibility(View.VISIBLE);
            viewModel.onGoogleClicked();
        });


    }

    /**
     * Observes LiveData from the ViewModel and updates the UI accordingly:
     * - Shows or hides error messages
     * - Displays loading state
     * - Navigates to the appropriate screen after successful login
     */
    private void observeViewModel() {
        // Observe error or info messages
        viewModel.getMessageResId().observe(this, msg -> {
            if (msg != null) {
                errorText.setText(msg);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        // Observe loading state and update UI accordingly
        viewModel.getIsLoading().observe(this, isLoading -> {
            googleBtn.setEnabled(!isLoading);
            googleBtn.setAlpha(isLoading ? 0.6f : 1f);

            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginBtn.setEnabled(!isLoading);
            loginBtn.setAlpha(isLoading ? 0.6f : 1f);
            registerBtn.setEnabled(!isLoading);
        });

        // Observe successful login and navigate based on user type
        viewModel.getUserType().observe(this, type -> {
            if (type == null) return;

            if (type == UserType.OWNER) {
                startActivity(new Intent(this, OwnerActivity.class));
            } else {
                startActivity(new Intent(this, ExploreEventsActivity.class));
            }

            finish();
        });

        viewModel.getLaunchGoogle().observe(this, ev -> {
            if (ev == null) return;


            ev.getContentIfNotHandled();

            googleLauncher.launch(googleClient.getSignInIntent());
        });



    }

    /**
     * Attaches text watchers to input fields in order to clear
     * the current error message when the user edits the input.
     */
    private void setupClearErrorOnInput() {
        usernameInput.addTextChangedListener(
                FormTextWatcher.after(text -> viewModel.clearMessage())
        );

        passwordInput.addTextChangedListener(
                FormTextWatcher.after(text -> viewModel.clearMessage())
        );
    }

}