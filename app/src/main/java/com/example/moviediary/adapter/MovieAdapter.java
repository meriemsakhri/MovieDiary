package com.example.moviediary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviediary.R;
import com.example.moviediary.database.DatabaseHelper;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Cursor cursor;
    private Context context;

    public MovieAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // Get data from cursor
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
        String posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
        String genre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE));
        int releaseYear = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RELEASE_YEAR));

        // Set data to views
        holder.movieTitle.setText(title);
        holder.movieDescription.setText(description);
        holder.movieGenre.setText(genre);
        holder.movieYear.setText(String.valueOf(releaseYear));

        // Load image using Glide with our placeholder
        if (posterUrl != null && !posterUrl.isEmpty() && !posterUrl.equals("https://example.com/inception.jpg")) {
            try {
                Glide.with(context)
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .error(R.drawable.ic_movie_placeholder)
                        .into(holder.movieImage);
            } catch (Exception e) {
                // If Glide fails, use placeholder
                holder.movieImage.setImageResource(R.drawable.ic_movie_placeholder);
            }
        } else {
            // placeholder for invalid URLs
            holder.movieImage.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }
    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView movieImage;
        TextView movieTitle;
        TextView movieGenre;
        TextView movieDescription;
        TextView movieYear;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage = itemView.findViewById(R.id.movie_image);
            movieTitle = itemView.findViewById(R.id.movie_title);
            movieGenre = itemView.findViewById(R.id.movie_genre);
            movieDescription = itemView.findViewById(R.id.movie_description);
            movieYear = itemView.findViewById(R.id.movie_year);
        }
    }
}