package DBUtils;

import Models.Movies;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class MoviesCollection {
    private static FindIterable<Document> movies_found;
    public static ArrayList<Movies> movies = new ArrayList<>();
    public static Movies movie;

    public MoviesCollection() {
        movies_found = DBConn.Movie_col.find().projection(Projections.fields(Projections.include("_id","title","rating"))).limit(500);
    }

    public static FindIterable<Document> allMovies(){
        return DBConn.Movie_col.find().limit(500);
    }

    public static ArrayList<Movies> GetAllMovies(FindIterable<Document> movs,String sortby, int order){
        if(sortby.isEmpty()){
            sortby="year";
        }
        BasicDBObject sort = new BasicDBObject(sortby, order);
        movs.sort(sort);
        return FillMoviesNew(movs);
    }

    public static ArrayList<Movies> FilterMovies(FindIterable<Document> movs,String searchIndex, String searchValue){
        BasicDBObject key = new BasicDBObject(searchIndex, new BasicDBObject("$regex",".*"+searchValue+"*."));
        movs.filter(key);
        return FillMoviesNew(movs);
    }

    public static ArrayList<Movies> FillMoviesNew(FindIterable<Document> moviesFound){
        movies = new ArrayList<>();
        movies.clear();
        try{
            for (Document doc : moviesFound){
                movie = new Movies(
                        String.valueOf(doc.get("_id")),
                        doc.getString("title"),
                        doc.getDouble("runtime"),
                        doc.getDouble("rating"),
                        doc.getDate("watch_date"),
                        doc.getString("watched")
                );
                if(movie != null) {
                    movies.add(movie);
                }
                else{
                    System.out.println("No Movies");
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return movies;
    }

    public static void InsertMovie(ArrayList<Object> Data){
        Document movie = new Document("_id", new ObjectId());
        movie.append("title",Data.get(0))
                .append("rating",(Double)Data.get(1))
                .append("runtime",(Double)Data.get(2))
                .append("watched",Data.get(3))
                .append("watch_date",(Date)Data.get(4));
        DBConn.getMovie_col().insertOne(movie);
    }

    public static void deleteMovie(String index, String value){
        System.out.println("To delete: "+value);
        DBConn.Movie_col.deleteOne(Filters.eq("title", value));
    }

    public static void UpdateMovie(String title, String status){
        System.out.println("Update Query: "+title+" : "+status);
        Document query = new Document();
        query.append("title",title);
        Document setData = new Document();
        setData.append("watched", status);
        setData.append("watch_date", new Date());
        Document update = new Document();
        update.append("$set", setData);
        //updating single Document
        DBConn.Movie_col.updateOne(query, update);
    }
}
