package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullName, password, confirmPassword, email, userName;
    private RadioButton owner;
    private FirebaseAuth auth;

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

        Button register = findViewById(R.id.btnRegister);
        Button backLogin = findViewById(R.id.btnAlreadyHaveAccount);
        backLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        register.setOnClickListener(v -> handleRegister());
    }
    private void handleRegister(){
        String emailText = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confPass = confirmPassword.getText().toString().trim();
        String fllName = fullName.getText().toString().trim();
        String userType = owner.isChecked() ? "owner" : "user";
        String username = userName.getText().toString().trim();

        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailText, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        assert auth.getCurrentUser() != null;
                        String uid = auth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, emailText, fllName, userType, username);

                    } else {
                        // בינתיים לא מטפלים בשגיאה
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String fullName, String userType, String userName) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("userName", userName);
        userData.put("userType", userType);

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {

                    // מעבר למסך מתאים
                    if (userType.equals("owner")) {
                        startActivity(new Intent(this, OwnerActivity.class));
                    } else {
                        startActivity(new Intent(this, ExploreEventsActivity.class));
                    }

                    finish();

                })
                .addOnFailureListener(e -> {
                    // כרגע לא מטפלים בשגיאה
                });
    }

}