package com.example.moviediary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MovieDiary.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_USER_MOVIES = "user_movies";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Users table columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PREFERENCES = "preferences";

    // Movies table columns
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_URL = "poster_url";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_RELEASE_YEAR = "release_year";
    public static final String COLUMN_GENRE = "genre";

    // User_Movies table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_IS_WATCHED = "is_watched";
    public static final String COLUMN_RATING = "rating";

    // Table creation queries
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL," +
                    COLUMN_PREFERENCES + " TEXT" + // Optional field
                    ")";

    private static final String CREATE_MOVIES_TABLE =
            "CREATE TABLE " + TABLE_MOVIES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_POSTER_URL + " TEXT," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_RELEASE_YEAR + " INTEGER," +
                    COLUMN_GENRE + " TEXT" + // New genre field
                    ")";

    private static final String CREATE_USER_MOVIES_TABLE =
            "CREATE TABLE " + TABLE_USER_MOVIES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER," +
                    COLUMN_MOVIE_ID + " INTEGER," +
                    COLUMN_IS_WATCHED + " INTEGER DEFAULT 0," +
                    COLUMN_RATING + " INTEGER DEFAULT 0," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")," +
                    "FOREIGN KEY(" + COLUMN_MOVIE_ID + ") REFERENCES " + TABLE_MOVIES + "(" + COLUMN_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MOVIES_TABLE);
        db.execSQL(CREATE_USER_MOVIES_TABLE);

        // Pre-populate with sample movies
        insertSampleMovies(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    private void insertSampleMovies(SQLiteDatabase db) {
        // Sample movies data with genres
        String[][] sampleMovies = {
                {"Inception", "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg", "A thief who steals corporate secrets through the use of dream-sharing technology.", "2010", "Sci-Fi, Action"},
                {"The Dark Knight", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg", "Batman faces the Joker, a criminal mastermind who seeks to undermine Batman's influence.", "2008", "Action, Crime, Drama"},
                {"Interstellar", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.", "2014", "Sci-Fi, Adventure, Drama"},
                {"The Shawshank Redemption", "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg", "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.", "1994", "Drama"},
                {"Pulp Fiction", "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg", "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.", "1994", "Crime, Drama"},
                {"The Godfather", "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg", "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.", "1972", "Crime, Drama"},
                {"Forrest Gump", "https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg", "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man.", "1994", "Drama, Romance"},
                {"The Matrix", "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg", "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.", "1999", "Action, Sci-Fi"}
        };

        for (String[] movie : sampleMovies) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, movie[0]);
            values.put(COLUMN_POSTER_URL, movie[1]);
            values.put(COLUMN_DESCRIPTION, movie[2]);
            values.put(COLUMN_RELEASE_YEAR, Integer.parseInt(movie[3]));
            values.put(COLUMN_GENRE, movie[4]);
            db.insert(TABLE_MOVIES, null, values);
        }
    }
}