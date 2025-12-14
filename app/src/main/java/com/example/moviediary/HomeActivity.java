package com.example.moviediary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviediary.adapter.MovieAdapter;
import com.example.moviediary.database.DatabaseManager;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MovieDiary");
        }

        dbManager = new DatabaseManager(this);
        dbManager.open();

        setupRecyclerView();
        loadMovies();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && !searchView.isIconified()) {
                    searchView.setIconified(true);
                } else {
                    setEnabled(false);
                    HomeActivity.super.onBackPressed();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Setup SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            setupSearchView();
        }

        // IMPORTANT: menu visibility handled in onPrepareOptionsMenu()
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu(); // refresh menu visibility
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean loggedIn = sessionManager.isLoggedIn();

        MenuItem profile = menu.findItem(R.id.action_profile);
        MenuItem logout = menu.findItem(R.id.action_logout);

        if (profile != null) profile.setVisible(loggedIn);
        if (logout != null) logout.setVisible(loggedIn);


        return super.onPrepareOptionsMenu(menu);
    }

    private void setupSearchView() {
        if (searchView == null) return;

        searchView.setQueryHint("Search movies...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    loadMovies();
                } else {
                    performSearch(newText.trim());
                }
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            loadMovies();
            return false;
        });
    }

    private void performSearch(String query) {
        Cursor cursor = dbManager.searchMovies(query);

        adapter.swapCursor(cursor);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No movies found for: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Found " + cursor.getCount() + " movies", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Go back to WelcomeActivity and clear back stack
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void loadMovies() {
        Cursor cursor = dbManager.getAllMovies();
        if (adapter == null) {
            adapter = new MovieAdapter(cursor);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}
