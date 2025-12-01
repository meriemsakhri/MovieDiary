package com.example.moviediary.model;

public class Movie {
    private int id;
    private String title;
    private String posterUrl;
    private String description;
    private int releaseYear;
    private String genre;

    // Constructors
    public Movie() {}

    public Movie(String title, String posterUrl, String description, int releaseYear, String genre) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.description = description;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}