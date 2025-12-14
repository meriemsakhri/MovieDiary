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

        sessionManager = new SessionManager(this);

        // If not logged in -> go to login
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Views
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPreferences = findViewById(R.id.tv_preferences);

        btnBackHome = findViewById(R.id.btn_back_home);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        // DB
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Load initial data
        loadUserData();

        // Back home
        btnBackHome.setOnClickListener(v -> goBackToHome());

        // EDIT -> open EditProfileActivity
        btnEditProfile.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(i);
        });

        // Logout
        btnLogout.setOnClickListener(v -> performLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh UI after editing profile
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadUserData();
        }
    }

    private void loadUserData() {
        // from session
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        int userId = sessionManager.getUserId();

        tvUsername.setText(username != null ? username : "");
        tvEmail.setText(email != null ? email : "");

        // preferences from DB
        String prefs = dbManager.getUserPreferences(userId);

        if (prefs == null || prefs.trim().isEmpty()) {
            tvPreferences.setText("No preferences set yet");
        } else {
            // Show nicely (replace commas with ", ")
            tvPreferences.setText(prefs.replace(",", ", "));
        }
    }

    private void goBackToHome() {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void performLogout() {
        sessionManager.logoutUser();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // go to Welcome page and clear back stack
        Intent i = new Intent(ProfileActivity.this, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}
