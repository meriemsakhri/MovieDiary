package com.example.moviediary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviediary.adapter.DiaryAdapter;
import com.example.moviediary.database.DatabaseManager;

public class MyDiaryActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private SessionManager sessionManager;

    private int userId;

    private DiaryAdapter wishlistAdapter;
    private DiaryAdapter watchedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_diary);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Diary");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        dbManager = new DatabaseManager(this);
        dbManager.open();

        RecyclerView rvWishlist = findViewById(R.id.rvWishlist);
        RecyclerView rvWatched = findViewById(R.id.rvWatched);

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        rvWatched.setLayoutManager(new LinearLayoutManager(this));

        wishlistAdapter = new DiaryAdapter(null, false, new DiaryAdapter.Listener() {
            @Override
            public void onOpenDetails(int movieId) {
                openDetails(movieId);
            }

            @Override
            public void onRemove(int movieId) {
                boolean ok = dbManager.removeFromDiary(userId, movieId);
                if (ok) Toast.makeText(MyDiaryActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                refresh();
            }
        });

        watchedAdapter = new DiaryAdapter(null, true, new DiaryAdapter.Listener() {
            @Override
            public void onOpenDetails(int movieId) {
                openDetails(movieId);
            }

            @Override
            public void onRemove(int movieId) {
                boolean ok = dbManager.removeFromDiary(userId, movieId);
                if (ok) Toast.makeText(MyDiaryActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                refresh();
            }
        });

        rvWishlist.setAdapter(wishlistAdapter);
        rvWatched.setAdapter(watchedAdapter);

        refresh();
    }

    private void openDetails(int movieId) {
        Intent i = new Intent(this, MovieDetailsActivity.class);
        i.putExtra("movie_id", movieId);
        startActivity(i);
    }

    private void refresh() {
        Cursor w1 = dbManager.getUserWishlistWithRating(userId);
        Cursor w2 = dbManager.getUserWatchedWithRating(userId);
        wishlistAdapter.swapCursor(w1);
        watchedAdapter.swapCursor(w2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbManager != null && userId != -1) refresh();
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
