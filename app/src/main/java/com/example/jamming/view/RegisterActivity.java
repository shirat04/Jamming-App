package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import com.example.jamming.model.UserType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullName, password, confirmPassword, email, userName;
    private RadioButton owner;
    private TextView errorText;
    private ProgressBar progressBar;
    private  Button register,backLogin;
    private RegisterViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupListeners();

        setupClearErrorOnInput(fullName, userName, email, password, confirmPassword);
        owner.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.clearError();
        });

        observeViewModel();
    }
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
    private void setupListeners() {
        backLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        register.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {

        String fName = fullName.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confPass = confirmPassword.getText().toString().trim();
        String uName = userName.getText().toString().trim();
        UserType type = owner.isChecked() ? UserType.OWNER : UserType.USER;

        viewModel.register(fName, emailTxt, pass, confPass, uName, type);
    }

    private void observeViewModel() {

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            register.setEnabled(!isLoading);
            backLogin.setEnabled(!isLoading);
            register.setAlpha(isLoading ? 0.6f : 1f);
        });



        viewModel.getErrorResId().observe(this, errResId -> {
            if (errResId != null) {
                errorText.setText(getString(errResId));
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.GONE);
            }
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

    private void setupClearErrorOnInput(EditText... fields) {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.clearError();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        for (EditText field : fields) {
            field.addTextChangedListener(watcher);
        }
    }

}