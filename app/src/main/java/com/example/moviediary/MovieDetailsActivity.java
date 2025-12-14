package com.example.moviediary;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.moviediary.database.DatabaseHelper;
import com.example.moviediary.database.DatabaseManager;
import com.example.moviediary.model.Movie;

public class MovieDetailsActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private SessionManager sessionManager;

    private int userId;
    private int movieId;

    private String currentStatus = null; // "WISHLIST" or "WATCHED" or null

    private Button btnWishlist, btnWatched;
    private LinearLayout ratingSection;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Movie Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId == -1) {
            Toast.makeText(this, "Movie not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbManager = new DatabaseManager(this);
        dbManager.open();

        ImageView imgPoster = findViewById(R.id.imgPoster);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvDescription = findViewById(R.id.tvDescription);

        btnWishlist = findViewById(R.id.btnWishlist);
        btnWatched = findViewById(R.id.btnWatched);
        ratingSection = findViewById(R.id.ratingSection);
        ratingBar = findViewById(R.id.ratingBar);

        // Load movie
        Movie movie = dbManager.getMovieById(movieId);
        if (movie == null) {
            Toast.makeText(this, "Movie not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(movie.getTitle());
        tvGenre.setText(movie.getGenre());
        tvYear.setText(String.valueOf(movie.getReleaseYear()));
        tvDescription.setText(movie.getDescription());

        Glide.with(this)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(imgPoster);

        // Load existing status/rating from DB
        loadDiaryStateAndUpdateUI();

        // Wishlist button: toggle add/remove wishlist
        btnWishlist.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("WISHLIST".equals(currentStatus)) {
                // remove from diary
                dbManager.removeFromDiary(userId, movieId);
                currentStatus = null;
                ratingBar.setRating(0);
            } else {
                // set wishlist (rating must be 0)
                currentStatus = "WISHLIST";
                dbManager.upsertDiary(userId, movieId, currentStatus, 0);
                ratingBar.setRating(0);
            }
            loadDiaryStateAndUpdateUI();
        });

        // Watched button: toggle add/remove watched
        btnWatched.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("WATCHED".equals(currentStatus)) {
                // remove from diary
                dbManager.removeFromDiary(userId, movieId);
                currentStatus = null;
                ratingBar.setRating(0);
            } else {
                // set watched (keep rating current)
                currentStatus = "WATCHED";
                int rating = (int) ratingBar.getRating();
                dbManager.upsertDiary(userId, movieId, currentStatus, rating);
            }
            loadDiaryStateAndUpdateUI();
        });

        // Auto-save rating ONLY when watched
        ratingBar.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (!fromUser) return;
            if (userId == -1) return;

            if ("WATCHED".equals(currentStatus)) {
                dbManager.upsertDiary(userId, movieId, "WATCHED", (int) rating);
                Toast.makeText(this, "Rating updated", Toast.LENGTH_SHORT).show();
            } else {
                // Not watched -> reset rating and hide section
                rb.setRating(0);
            }
        });
    }

    private void loadDiaryStateAndUpdateUI() {
        // default
        currentStatus = null;
        int savedRating = 0;

        if (userId != -1) {
            Cursor c = dbManager.getDiaryRow(userId, movieId);
            if (c != null && c.moveToFirst()) {
                currentStatus = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS));
                savedRating = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATING));
                c.close();
            } else {
                if (c != null) c.close();
            }
        }

        // Update buttons UI (selected color + text)
        updateButtonsUI();

        // Rating section visible only if watched
        if ("WATCHED".equals(currentStatus)) {
            ratingSection.setVisibility(View.VISIBLE);
            ratingBar.setRating(savedRating);
        } else {
            ratingSection.setVisibility(View.GONE);
            ratingBar.setRating(0);
        }
    }

    private void updateButtonsUI() {
        // reset styles
        setButtonSelected(btnWishlist, false);
        setButtonSelected(btnWatched, false);

        btnWishlist.setText("Wishlist");
        btnWatched.setText("Watched");

        if ("WISHLIST".equals(currentStatus)) {
            setButtonSelected(btnWishlist, true);
            btnWishlist.setText("Remove from wishlist");
        } else if ("WATCHED".equals(currentStatus)) {
            setButtonSelected(btnWatched, true);
            btnWatched.setText("Remove from watched");
        }
    }

    private void setButtonSelected(Button btn, boolean selected) {
        if (selected) {
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
            btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            btn.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
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
