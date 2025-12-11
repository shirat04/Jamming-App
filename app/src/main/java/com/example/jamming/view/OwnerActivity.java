package com.example.jamming.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jamming.R;

public class OwnerActivity extends AppCompatActivity {
    private Button createEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerActivity.this, CreateNewEvent.class);
            startActivity(intent);
        });


    }

}