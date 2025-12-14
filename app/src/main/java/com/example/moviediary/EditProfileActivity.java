package com.example.moviediary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviediary.database.DatabaseManager;

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername, etEmail;
    private CheckBox cbAction, cbDrama, cbComedy, cbCrime, cbRomance, cbSciFi, cbAdventure;

    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        dbManager = new DatabaseManager(this);
        dbManager.open();

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);

        cbAction = findViewById(R.id.cbAction);
        cbDrama = findViewById(R.id.cbDrama);
        cbComedy = findViewById(R.id.cbComedy);
        cbCrime = findViewById(R.id.cbCrime);
        cbRomance = findViewById(R.id.cbRomance);
        cbSciFi = findViewById(R.id.cbSciFi);
        cbAdventure = findViewById(R.id.cbAdventure);

        Button btnSave = findViewById(R.id.btnSaveProfile);

        // Prefill from session
        etUsername.setText(sessionManager.getUsername());
        etEmail.setText(sessionManager.getEmail());

        // Prefill checkboxes from DB preferences string
        String prefs = dbManager.getUserPreferences(userId); // we'll add this method
        applyPrefsToCheckboxes(prefs);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String prefs = buildPrefsString();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Username and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbManager.isUsernameExistsForOtherUser(newUsername, userId)) {
            Toast.makeText(this, "Username already used", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbManager.isEmailExistsForOtherUser(newEmail, userId)) {
            Toast.makeText(this, "Email already used", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbManager.updateUserProfile(userId, newUsername, newEmail, prefs);
        if (ok) {
            sessionManager.updateSession(newUsername, newEmail);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildPrefsString() {
        ArrayList<String> list = new ArrayList<>();
        if (cbAction.isChecked()) list.add("Action");
        if (cbDrama.isChecked()) list.add("Drama");
        if (cbComedy.isChecked()) list.add("Comedy");
        if (cbCrime.isChecked()) list.add("Crime");
        if (cbRomance.isChecked()) list.add("Romance");
        if (cbSciFi.isChecked()) list.add("Sci-Fi");
        if (cbAdventure.isChecked()) list.add("Adventure");

        // stored as: "Action,Drama,Sci-Fi"
        return android.text.TextUtils.join(",", list);
    }

    private void applyPrefsToCheckboxes(String prefs) {
        if (prefs == null) return;

        String p = prefs.toLowerCase();

        cbAction.setChecked(p.contains("action"));
        cbDrama.setChecked(p.contains("drama"));
        cbComedy.setChecked(p.contains("comedy"));
        cbCrime.setChecked(p.contains("crime"));
        cbRomance.setChecked(p.contains("romance"));
        cbSciFi.setChecked(p.contains("sci-fi") || p.contains("scifi"));
        cbAdventure.setChecked(p.contains("adventure"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}
