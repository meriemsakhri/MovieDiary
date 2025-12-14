package com.example.moviediary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.moviediary.model.User;
import com.example.moviediary.model.Movie;
import com.example.moviediary.security.PasswordUtils;


public class DatabaseManager {
    private DatabaseHelper dbHelper;
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

    // USER OPERATIONS

    /**
     * Add a new user to the database
     */
    public boolean addUser(User user) {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(user.getPassword(), salt);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_PASSWORD_HASH, hash);
        values.put(DatabaseHelper.COLUMN_SALT, salt);
        values.put(DatabaseHelper.COLUMN_PREFERENCES, user.getPreferences());

        long result = database.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1;
    }


    /**
     * Get user by email and password (for login)
     */
    public User getUser(String email, String password) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String salt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SALT));
            String hash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH));

            boolean ok = PasswordUtils.verifyPassword(password, salt, hash);

            if (ok) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
                user.setPreferences(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREFERENCES)));
                cursor.close();
                return user;
            }
        }

        if (cursor != null) cursor.close();
        return null;
    }


    /**
     * Check if email already exists
     */
    public boolean isEmailExists(String email) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Check if username already exists
     */
    public boolean isUsernameExists(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    // MOVIE OPERATIONS

    /**
     * Get all movies for the home screen
     */
    public Cursor getAllMovies() {
        return database.query(
                DatabaseHelper.TABLE_MOVIES,
                null, // all columns
                null, // where clause
                null, // where arguments
                null, // group by
                null, // having
                DatabaseHelper.COLUMN_TITLE + " ASC" // order by
        );
    }

    /**
     * Get movies by genre
     */
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

    /**
     * Get movie by ID
     */
    public Movie getMovieById(int movieId) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_MOVIES,
                null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Movie movie = new Movie();
            movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
            movie.setPosterUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL)));
            movie.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
            movie.setReleaseYear(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RELEASE_YEAR)));
            movie.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE)));
            cursor.close();
            return movie;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    // USER_MOVIES OPERATIONS (for later use)

    /**
     * Add movie to user's wishlist
     */
    public boolean addToWishlist(int userId, int movieId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_MOVIE_ID, movieId);
        values.put(DatabaseHelper.COLUMN_IS_WATCHED, 0); // 0 for wishlist
        values.put(DatabaseHelper.COLUMN_RATING, 0); // No rating yet

        long result = database.insert(DatabaseHelper.TABLE_USER_MOVIES, null, values);
        return result != -1;
    }

    /**
     * Get user's wishlist
     */
    public Cursor getUserWishlist(int userId) {
        String query = "SELECT m.* FROM " + DatabaseHelper.TABLE_MOVIES + " m " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER_MOVIES + " um " +
                "ON m." + DatabaseHelper.COLUMN_ID + " = um." + DatabaseHelper.COLUMN_MOVIE_ID + " " +
                "WHERE um." + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                "AND um." + DatabaseHelper.COLUMN_IS_WATCHED + " = 0";

        return database.rawQuery(query, new String[]{String.valueOf(userId)});
    }
    /**
     * Search movies by title or genre
     */
    public Cursor searchMovies(String query) {
        String searchQuery = "%" + query + "%";

        return database.query(
                DatabaseHelper.TABLE_MOVIES,
                null, // all columns
                DatabaseHelper.COLUMN_TITLE + " LIKE ? OR " +
                        DatabaseHelper.COLUMN_GENRE + " LIKE ? OR " +
                        DatabaseHelper.COLUMN_DESCRIPTION + " LIKE ?",
                new String[]{searchQuery, searchQuery, searchQuery},
                null, // group by
                null, // having
                DatabaseHelper.COLUMN_TITLE + " ASC" // order by
        );
    }
}