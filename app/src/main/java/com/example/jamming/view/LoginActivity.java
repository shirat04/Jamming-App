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

public class LoginActivity extends AppCompatActivity {


    private EditText usernameInput, passwordInput;
    private LoginViewModel loginViewModel;
    ProgressBar progressBar;

    private Button loginBtn,registerBtn;
    private TextView errorText, forgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        registerBtn = findViewById(R.id.registerText);
        errorText = findViewById(R.id.errorTextView);
        forgotPassword = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.loginProgressBar);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        TextWatcher clearMessageWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.clearMessage();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        usernameInput.addTextChangedListener(clearMessageWatcher);
        passwordInput.addTextChangedListener(clearMessageWatcher);

        loginBtn.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            loginViewModel.login(identifier, pass);
        });

        forgotPassword.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            loginViewModel.resetPassword(identifier);
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        observeViewModel();
    }

    private void observeViewModel() {
        loginViewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                errorText.setText(msg);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                loginBtn.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
            }
        });

        loginViewModel.getIsLoading().observe(this, isLoading -> {
            loginBtn.setEnabled(!isLoading);
            loginBtn.setAlpha(isLoading ? 0.6f : 1f);
        });


        loginViewModel.getUserType().observe(this, type -> {
            if (type == null) return;

            if ("owner".equals(type)) {
                startActivity(new Intent(this, OwnerActivity.class));
            } else {
                startActivity(new Intent(this, ExploreEventsActivity.class));
            }

            finish();
        });
    }
}