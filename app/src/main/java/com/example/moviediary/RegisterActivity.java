package com.example.moviediary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviediary.database.DatabaseManager;
import com.example.moviediary.model.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // HIDE ACTION BAR IN REGISTER ACTIVITY
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize database manager
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Initialize views
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);


        TextView tvLogin = findViewById(R.id.tv_login_link);

        // Setup click listeners
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });



        // Setup login link click listener
        if (tvLogin != null) {
            tvLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Login Activity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            // If the ID doesn't work, let's find it by text (fallback)
            Toast.makeText(this, "Please click back button to go to login", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists
        if (dbManager.isUsernameExists(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email already exists
        if (dbManager.isEmailExists(email)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new user
        User user = new User(username, email, password);
        boolean success = dbManager.addUser(user);

        if (success) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

            // Go back to login activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackToHome() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Optional: removes RegisterActivity from back stack
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}