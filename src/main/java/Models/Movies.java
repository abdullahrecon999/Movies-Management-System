package Models;

import org.bson.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Movies implements Serializable {
    private String Movie_id;
    private String Title;
    private Double Runtime;
    private double Rating;
    private Date WatchedDate;
    private String Watched;

    public Movies(String movie_id, String title, Double runtime, double rating, Date watchedDate, String watched) {
        Movie_id = movie_id;
        Title = title;
        Runtime = runtime;
        Rating = rating;
        WatchedDate = watchedDate;
        Watched = watched;
    }

    public Date getWatchedDate() {
        return WatchedDate;
    }

    public String isWatched() {
        return Watched;
    }

    public String getMovie_id() {
        return Movie_id;
    }

    public String getTitle() {
        return Title;
    }

    public Double getRating() {
        return Rating;
    }

    public Double getRuntime() {
        return Runtime;
    }
}

