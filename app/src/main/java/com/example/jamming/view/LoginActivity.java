package com.example.jamming.view;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
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

public class LoginActivity extends AppCompatActivity {


    private EditText usernameInput, passwordInput;
    private LoginViewModel viewModel;
    ProgressBar progressBar;

    private Button loginBtn,registerBtn;
    private TextView errorText, forgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupClearErrorOnInput();

        setupListeners();

        observeViewModel();
    }
    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        registerBtn = findViewById(R.id.registerText);
        errorText = findViewById(R.id.errorTextView);
        forgotPassword = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.loginProgressBar);
    }

    private void setupListeners() {
        loginBtn.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            viewModel.login(identifier, pass);
        });

        forgotPassword.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            viewModel.resetPassword(identifier);
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

        private void observeViewModel() {
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                errorText.setText(msg);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginBtn.setEnabled(!isLoading);
            loginBtn.setAlpha(isLoading ? 0.6f : 1f);
        });



        viewModel.getUserType().observe(this, type -> {
            if (type == null) return;

            if (type == UserType.OWNER) {
                startActivity(new Intent(this, OwnerActivity.class));
            } else {
                startActivity(new Intent(this, ExploreEventsActivity.class));
            }

            finish();
        });

    }

    private void setupClearErrorOnInput() {
        TextWatcher clearMessageWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.clearMessage();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        usernameInput.addTextChangedListener(clearMessageWatcher);
        passwordInput.addTextChangedListener(clearMessageWatcher);

    }
}