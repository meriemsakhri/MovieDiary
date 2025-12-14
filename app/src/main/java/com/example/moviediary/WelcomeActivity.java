package com.example.moviediary;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class WelcomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // HIDE ACTION BAR IN REGISTER ACTIVITY
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sessionManager = new SessionManager(this);

        // If already logged in, skip welcome and go home
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
