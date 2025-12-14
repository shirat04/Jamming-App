package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jamming.R;
import com.example.jamming.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullName, password, confirmPassword, email, userName;
    private RadioButton owner;
    private TextView errorText;

    private RegisterViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fullName = findViewById(R.id.etName);
        password = findViewById(R.id.etPassword);
        confirmPassword = findViewById(R.id.etConfirmPassword);
        email = findViewById(R.id.etEmail);
        owner = findViewById(R.id.rbOwner);
        userName = findViewById(R.id.usName);
        errorText = findViewById(R.id.errorText);

        Button register = findViewById(R.id.btnRegister);
        Button backLogin = findViewById(R.id.btnAlreadyHaveAccount);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        backLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        register.setOnClickListener(v -> handleRegister());

        observeViewModel();
    }
    private void handleRegister() {

        String fName = fullName.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confPass = confirmPassword.getText().toString().trim();
        String uName = userName.getText().toString().trim();
        String type = owner.isChecked() ? "owner" : "user";

        viewModel.register(fName, emailTxt, pass, confPass, uName, type);
    }

    private void observeViewModel() {

        viewModel.getError().observe(this, err -> {
            if (err != null) {
                errorText.setText(err);
                errorText.setVisibility(View.VISIBLE);
            } else {
                errorText.setVisibility(View.GONE);
            }
        });

        viewModel.getUserType().observe(this, type -> {
            if (type == null) return;

            if (type.equals("owner")) {
                startActivity(new Intent(this, OwnerActivity.class));
            } else {
                startActivity(new Intent(this, ExploreEventsActivity.class));
            }
            finish();
        });
    }

}