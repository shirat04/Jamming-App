package com.example.jamming.view;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jamming.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    private EditText username, password;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginButton);
        Button registerBtn = findViewById(R.id.registerText);
        auth = FirebaseAuth.getInstance();
        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);});
        loginBtn.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
        //למלא פה
        return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        assert auth.getCurrentUser() != null;
                        String uid = auth.getCurrentUser().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        String type = doc.getString("userType");

                                        if ("owner".equals(type)) {
                                            startActivity(new Intent(this, OwnerActivity.class));
                                        } else {
                                            startActivity(new Intent(this, ExploreEventsActivity.class));
                                        }

                                        finish();
                                    } else {
                                        //לטפל
                                    }
                                });

                    } else {
                        //לטפל
                    }
                });
    }
}
