package com.example.jamming.view;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jamming.R;
import com.example.jamming.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {


    private EditText usernameInput, passwordInput;
    private AuthViewModel viewModel;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);   // יכול להיות אימייל או username
        passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginButton);
        Button registerBtn = findViewById(R.id.registerText);
        ProgressBar progress = findViewById(R.id.loginProgressBar);


        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loginBtn.setOnClickListener(v -> {
            String identifier = usernameInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();
            viewModel.login(identifier, pass);
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        observeViewModel();
    }

    private void observeViewModel() {

        viewModel.getError().observe(this, err -> {
            if (err != null) {
                // כרגע אפשר רק להדפיס או Toast, ואת יכולה ללטש אחר כך
                // Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUserType().observe(this, type -> {
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