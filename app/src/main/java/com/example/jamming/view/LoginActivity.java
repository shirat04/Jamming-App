package com.example.jamming.view;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {


    private EditText usernameInput, passwordInput;
    private LoginViewModel loginViewModel;

    private TextView errorText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);   // יכול להיות אימייל או username
        passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginButton);
        Button registerBtn = findViewById(R.id.registerText);
        errorText = findViewById(R.id.errorTextView);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginBtn.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            loginViewModel.login(identifier, pass);
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        observeViewModel();
    }

    private void observeViewModel() {

        loginViewModel.getError().observe(this, err -> {
            if (err != null) {
                errorText.setText(err);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.GONE);
            }
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