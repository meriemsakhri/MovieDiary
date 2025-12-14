package com.example.moviediary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.moviediary.model.Movie;
import com.example.moviediary.model.User;
import com.example.moviediary.security.PasswordUtils;

public class DatabaseManager {

    // Aliases used in JOIN queries
    public static final String ALIAS_USER_STATUS = "user_status";
    public static final String ALIAS_USER_RATING = "user_rating";

    // Status values (keep consistent everywhere)
    public static final String STATUS_WISHLIST = DatabaseHelper.STATUS_WISHLIST; // "WISHLIST"
    public static final String STATUS_WATCHED = DatabaseHelper.STATUS_WATCHED;   // "WATCHED"

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // =======================
    // USER OPERATIONS
    // =======================

    public boolean addUser(User user) {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(user.getPassword(), salt);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, hash);
        values.put(DatabaseHelper.COLUMN_SALT, salt);
        values.put(DatabaseHelper.COLUMN_PREFERENCES, user.getPreferences());

        return database.insert(DatabaseHelper.TABLE_USERS, null, values) != -1;
    }

    /**
     * Login: find user by email, verify hashed password.
     */
    public User getUser(String email, String password) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SALT));
                String hash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH));

                if (PasswordUtils.verifyPassword(password, salt, hash)) {
                    User user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                    user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
                    user.setPreferences(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREFERENCES)));
                    return user;
                }
            }
            return null;
        } finally {
            closeQuietly(cursor);
        }
    }

    public boolean isEmailExists(String email) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            closeQuietly(cursor);
        }
    }

    public boolean isUsernameExists(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );
        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            closeQuietly(cursor);
        }
    }

    // =======================
    // MOVIE OPERATIONS
    // =======================

    public Cursor getAllMovies() {
        return database.query(
                DatabaseHelper.TABLE_MOVIES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_TITLE + " ASC"
        );
    }

    public Cursor getMoviesByGenre(String genre) {
        return database.query(
                DatabaseHelper.TABLE_MOVIES,
                null,
                DatabaseHelper.COLUMN_GENRE + " LIKE ?",
                new String[]{"%" + genre + "%"},
                null, null,
                DatabaseHelper.COLUMN_TITLE + " ASC"
        );
    }

    public Movie getMovieById(int movieId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_MOVIES,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null, null, null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
                movie.setPosterUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL)));
                movie.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
                movie.setReleaseYear(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RELEASE_YEAR)));
                movie.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE)));
                return movie;
            }
            return null;
        } finally {
            closeQuietly(cursor);
        }
    }

    public Cursor searchMovies(String query) {
        String like = likeQuery(query);

        return database.query(
                DatabaseHelper.TABLE_MOVIES,
                null,
                DatabaseHelper.COLUMN_TITLE + " LIKE ? OR " +
                        DatabaseHelper.COLUMN_GENRE + " LIKE ? OR " +
                        DatabaseHelper.COLUMN_DESCRIPTION + " LIKE ?",
                new String[]{like, like, like},
                null,
                null,
                DatabaseHelper.COLUMN_TITLE + " ASC"
        );
    }

    // =======================
    // DIARY (user_movies) OPERATIONS
    // One row per (user_id, movie_id)
    // status: WISHLIST / WATCHED
    // rating: 0..5
    // =======================

    public boolean upsertDiary(int userId, int movieId, String status, int rating) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_MOVIE_ID, movieId);
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        values.put(DatabaseHelper.COLUMN_RATING, rating);

        long result = database.insertWithOnConflict(
                DatabaseHelper.TABLE_USER_MOVIES,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        return result != -1;
    }

    public Cursor getDiaryRow(int userId, int movieId) {
        return database.query(
                DatabaseHelper.TABLE_USER_MOVIES,
                null,
                DatabaseHelper.COLUMN_USER_ID + "=? AND " + DatabaseHelper.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)},
                null, null, null
        );
    }

    public Cursor getUserWatchlist(int userId) {
        // kept for compatibility, but it matches STATUS_WISHLIST in this project
        return getUserWishlistWithRating(userId);
    }

    public Cursor getUserWatched(int userId) {
        String q = "SELECT m.* FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                "JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID + " " +
                "WHERE um." + DatabaseHelper.COLUMN_USER_ID + "=? " +
                "AND um." + DatabaseHelper.COLUMN_STATUS + "=? " +
                "ORDER BY m." + DatabaseHelper.COLUMN_TITLE + " ASC";
        return database.rawQuery(q, new String[]{String.valueOf(userId), STATUS_WATCHED});
    }

    public boolean removeFromDiary(int userId, int movieId) {
        int rows = database.delete(
                DatabaseHelper.TABLE_USER_MOVIES,
                DatabaseHelper.COLUMN_USER_ID + "=? AND " + DatabaseHelper.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)}
        );
        return rows > 0;
    }

    public Cursor getUserWishlistWithRating(int userId) {
        String q = "SELECT m.*, um." + DatabaseHelper.COLUMN_RATING + " AS " + ALIAS_USER_RATING + " " +
                "FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                "JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID + " " +
                "WHERE um." + DatabaseHelper.COLUMN_USER_ID + "=? " +
                "AND um." + DatabaseHelper.COLUMN_STATUS + "=? " +
                "ORDER BY m." + DatabaseHelper.COLUMN_TITLE + " ASC";
        return database.rawQuery(q, new String[]{String.valueOf(userId), STATUS_WISHLIST});
    }

    public Cursor getUserWatchedWithRating(int userId) {
        String q = "SELECT m.*, um." + DatabaseHelper.COLUMN_RATING + " AS " + ALIAS_USER_RATING + " " +
                "FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                "JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID + " " +
                "WHERE um." + DatabaseHelper.COLUMN_USER_ID + "=? " +
                "AND um." + DatabaseHelper.COLUMN_STATUS + "=? " +
                "ORDER BY m." + DatabaseHelper.COLUMN_TITLE + " ASC";
        return database.rawQuery(q, new String[]{String.valueOf(userId), STATUS_WATCHED});
    }

    public Cursor getMoviesForHome(int userId) {
        String query =
                "SELECT m.*, " +
                        "um." + DatabaseHelper.COLUMN_STATUS + " AS " + ALIAS_USER_STATUS + ", " +
                        "um." + DatabaseHelper.COLUMN_RATING + " AS " + ALIAS_USER_RATING + " " +
                        "FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                        "LEFT JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                        "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID +
                        " AND um." + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                        "ORDER BY m." + DatabaseHelper.COLUMN_TITLE + " ASC";

        return database.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getMoviesForHomePersonalized(int userId, String preferencesCsv) {
        if (preferencesCsv == null || preferencesCsv.trim().isEmpty()) {
            return getMoviesForHome(userId);
        }

        String[] prefList = preferencesCsv.trim().split(",");
        StringBuilder orderCase = new StringBuilder("CASE ");

        int weight = 1;
        for (String p : prefList) {
            String genre = p.trim();
            if (!genre.isEmpty()) {
                orderCase.append("WHEN m.")
                        .append(DatabaseHelper.COLUMN_GENRE)
                        .append(" LIKE '%")
                        .append(genre.replace("'", "''"))
                        .append("%' THEN ")
                        .append(weight)
                        .append(" ");
                weight++;
            }
        }

        orderCase.append("ELSE 999 END, m.")
                .append(DatabaseHelper.COLUMN_TITLE)
                .append(" ASC");

        String query =
                "SELECT m.*, " +
                        "um." + DatabaseHelper.COLUMN_STATUS + " AS " + ALIAS_USER_STATUS + ", " +
                        "um." + DatabaseHelper.COLUMN_RATING + " AS " + ALIAS_USER_RATING + " " +
                        "FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                        "LEFT JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                        "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID +
                        " AND um." + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                        "ORDER BY " + orderCase;

        return database.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // =======================
    // PREFERENCES + PROFILE UPDATE
    // =======================

    public String getUserPreferences(int userId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_PREFERENCES},
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                String prefs = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREFERENCES));
                return prefs == null ? "" : prefs;
            }
            return "";
        } finally {
            closeQuietly(cursor);
        }
    }

    public boolean updateUserProfile(int userId, String newUsername, String newEmail, String newPreferences) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);
        values.put(DatabaseHelper.COLUMN_EMAIL, newEmail);
        values.put(DatabaseHelper.COLUMN_PREFERENCES, newPreferences);

        int rows = database.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
        return rows > 0;
    }

    public boolean isEmailExistsForOtherUser(String email, int userId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_EMAIL + "=? AND " + DatabaseHelper.COLUMN_ID + "!=?",
                new String[]{email, String.valueOf(userId)},
                null, null, null
        );

        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            closeQuietly(cursor);
        }
    }

    public boolean isUsernameExistsForOtherUser(String username, int userId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_ID + "!=?",
                new String[]{username, String.valueOf(userId)},
                null, null, null
        );

        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            closeQuietly(cursor);
        }
    }

    // =======================
    // Helpers
    // =======================

    private String likeQuery(String q) {
        return "%" + (q == null ? "" : q.trim()) + "%";
    }

    private void closeQuietly(Cursor c) {
        if (c != null) c.close();
    }
}
