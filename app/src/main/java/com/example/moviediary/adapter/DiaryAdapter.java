package com.example.moviediary.adapter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviediary.R;
import com.example.moviediary.database.DatabaseHelper;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    public interface Listener {
        void onOpenDetails(int movieId);
        void onRemove(int movieId);
    }

    private static final String COL_USER_RATING_ALIAS = "user_rating";

    private Cursor cursor;
    private final boolean showRating; // true for watched list
    private final Listener listener;

    public DiaryAdapter(Cursor cursor, boolean showRating, Listener listener) {
        this.cursor = cursor;
        this.showRating = showRating;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_movie, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return;

        int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
        String genre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RELEASE_YEAR));
        String posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL));

        int rating = getIntSafely(cursor, COL_USER_RATING_ALIAS, 0);

        h.tvTitle.setText(title);
        h.tvGenre.setText(genre);
        h.tvYear.setText(String.valueOf(year));

        Glide.with(h.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(h.imgPoster);

        if (showRating) {
            h.ratingContainer.setVisibility(View.VISIBLE);
            h.ratingBarSmall.setRating(rating);
        } else {
            h.ratingContainer.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onOpenDetails(movieId));
        h.btnRemove.setOnClickListener(v -> listener.onRemove(movieId));
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

    private int getIntSafely(Cursor c, String columnName, int defaultValue) {
        int idx = c.getColumnIndex(columnName);
        if (idx == -1 || c.isNull(idx)) return defaultValue;
        return c.getInt(idx);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgPoster;
        final TextView tvTitle;
        final TextView tvGenre;
        final TextView tvYear;
        final LinearLayout ratingContainer;
        final RatingBar ratingBarSmall;
        final Button btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvYear = itemView.findViewById(R.id.tvYear);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
            ratingBarSmall = itemView.findViewById(R.id.ratingBarSmall);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
