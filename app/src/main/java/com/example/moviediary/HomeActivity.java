package com.example.moviediary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviediary.adapter.MovieAdapter;
import com.example.moviediary.database.DatabaseManager;
import androidx.activity.OnBackPressedCallback;
public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private SearchView searchView; // ADD THIS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Set title in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("MovieDiary");
        }

        // Initialize database manager
        dbManager = new DatabaseManager(this);
        dbManager.open();

        // Setup RecyclerView
        setupRecyclerView();

        // Load movies
        loadMovies();
        // Setup back press handler for search view - ADD THIS AT THE END
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && !searchView.isIconified()) {
                    searchView.setIconified(true);
                } else {
                    // If search is not open, use default back behavior
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
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView();

        // Show/hide menu items based on login status
        if (sessionManager.isLoggedIn()) {
            // User is logged in - show Profile and Logout
            menu.findItem(R.id.action_login).setVisible(false);
            menu.findItem(R.id.action_register).setVisible(false);
            menu.findItem(R.id.action_profile).setVisible(true);
            menu.findItem(R.id.action_logout).setVisible(true);
        } else {
            // User is not logged in - show Login and Register
            menu.findItem(R.id.action_login).setVisible(true);
            menu.findItem(R.id.action_register).setVisible(true);
            menu.findItem(R.id.action_profile).setVisible(false);
            menu.findItem(R.id.action_logout).setVisible(false);
        }

        return true;
    }

    private void setupSearchView() {
        if (searchView == null) return;

        // Configure the search info and add any event listeners
        searchView.setQueryHint("Search movies...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // When user presses search button on keyboard
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // When text changes (real-time search)
                if (newText.isEmpty()) {
                    // If search is empty, show all movies
                    loadMovies();
                } else {
                    // Perform search as user types
                    performSearch(newText);
                }
                return true;
            }
        });

        // When search is closed, show all movies
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadMovies();
                return false;
            }
        });
    }

    private void performSearch(String query) {
        // Search movies by title or genre
        Cursor cursor = dbManager.searchMovies(query);

        if (cursor == null || cursor.getCount() == 0) {
            // No results found
            adapter.swapCursor(cursor);
            Toast.makeText(this, "No movies found for: " + query, Toast.LENGTH_SHORT).show();
        } else {
            // Update adapter with search results
            adapter.swapCursor(cursor);
            Toast.makeText(this, "Found " + cursor.getCount() + " movies", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_register) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            // Go to Profile Activity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Logout user
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Refresh the activity to update menu
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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
        if (dbManager != null) {
            dbManager.close();
        }
    }
}