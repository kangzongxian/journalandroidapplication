package com.example.self;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the elevation of the top bar to 0
//        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        // Get the button, set it such that when click, go to Login Page
        getStartedButton = findViewById(R.id.startButton);

        // Go from MainActivity page to Login page
        getStartedButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }
}