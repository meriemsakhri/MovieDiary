package com.example.moviediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.moviediary.database.DatabaseManager;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvUsername, tvEmail, tvPreferences;
    private Button btnBackHome, btnEditProfile, btnLogout;
    private SessionManager sessionManager;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // HIDE ACTION BAR IN PROFILE ACTIVITY
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check if user is logged in
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPreferences = findViewById(R.id.tv_preferences);
        btnBackHome = findViewById(R.id.btn_back_home);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout); // INITIALIZE LOGOUT BUTTON

        // Initialize database
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Load and display user data
        loadUserData();

        // Setup click listeners
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToHome();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // LOGOUT BUTTON LISTENER - ADD THIS
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });
    }

    private void loadUserData() {
        // Get data from session
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();

        // Display user data
        tvUsername.setText(username);
        tvEmail.setText(email);
        tvPreferences.setText("No preferences set yet");
    }

    private void goBackToHome() {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void performLogout() {
        // Clear the session
        sessionManager.logoutUser();

        // Show confirmation message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();


        // go to Welcome page  and clear back stack
        Intent i = new Intent(ProfileActivity.this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish(); // close ProfileActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}