package com.example.moviediary.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviediary.MovieDetailsActivity;
import com.example.moviediary.R;
import com.example.moviediary.database.DatabaseHelper;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String COL_USER_STATUS_ALIAS = "user_status";
    private static final String COL_USER_RATING_ALIAS = "user_rating";

    private static final String STATUS_WATCHED = "WATCHED";
    private static final String STATUS_WISHLIST = "WISHLIST";

    private Cursor cursor;

    public MovieAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return;

        // Basic movie data
        int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
        String posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
        String genre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE));
        int releaseYear = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RELEASE_YEAR));

        holder.movieTitle.setText(title);
        holder.movieDescription.setText(description);
        holder.movieGenre.setText(genre);
        holder.movieYear.setText(String.valueOf(releaseYear));

        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(holder.movieImage);

        // User-specific state (only present when using the personalized home query)
        String userStatus = getStringSafely(cursor, COL_USER_STATUS_ALIAS, null);
        int userRating = getIntSafely(cursor, COL_USER_RATING_ALIAS, 0);

        // Reset recycled views
        holder.itemView.setAlpha(1f);
        holder.imgWishlist.setVisibility(View.GONE);
        holder.ratingBar.setVisibility(View.GONE);
        holder.ratingBar.setRating(0);

        // Apply UI rules
        if (STATUS_WATCHED.equals(userStatus)) {
            holder.itemView.setAlpha(0.6f);
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(userRating);
        } else if (STATUS_WISHLIST.equals(userStatus)) {
            holder.imgWishlist.setVisibility(View.VISIBLE);
        }

        // Click → Movie Details
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(holder.itemView.getContext(), MovieDetailsActivity.class);
            i.putExtra("movie_id", movieId);
            holder.itemView.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    private String getStringSafely(Cursor c, String columnName, String defaultValue) {
        int idx = c.getColumnIndex(columnName);
        if (idx == -1 || c.isNull(idx)) return defaultValue;
        return c.getString(idx);
    }

    private int getIntSafely(Cursor c, String columnName, int defaultValue) {
        int idx = c.getColumnIndex(columnName);
        if (idx == -1 || c.isNull(idx)) return defaultValue;
        return c.getInt(idx);
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        final ImageView movieImage;
        final ImageView imgWishlist;
        final RatingBar ratingBar;

        final TextView movieTitle;
        final TextView movieGenre;
        final TextView movieDescription;
        final TextView movieYear;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);

            movieImage = itemView.findViewById(R.id.movie_image);
            imgWishlist = itemView.findViewById(R.id.imgWishlist);
            ratingBar = itemView.findViewById(R.id.ratingBarCard);

            movieTitle = itemView.findViewById(R.id.movie_title);
            movieGenre = itemView.findViewById(R.id.movie_genre);
            movieDescription = itemView.findViewById(R.id.movie_description);
            movieYear = itemView.findViewById(R.id.movie_year);
        }
    }
}
