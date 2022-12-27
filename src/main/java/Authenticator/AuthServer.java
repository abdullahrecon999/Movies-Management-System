package Authenticator;

import Client.User;
import DBUtils.DBConn;
import Server.ServerCon;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.sun.tools.javac.Main;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.net.*;
import java.io.*;
import java.util.Date;

public class AuthServer {

    String clientAuths;
    String ResponseMsg;
    boolean check = false;
    DataOutputStream outToClient;
    MongoCollection<Document> dbcol;

    public AuthServer(int port) throws IOException {
        dbcol = DBGetConn();
        ServerSocket NewSocket = new ServerSocket(1234);
        while (true) {
            Socket connectionSocket = NewSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientAuths = inFromClient.readLine();
            //System.out.println("Received: " + clientAuths);

            // Add check whether packet if LOGIN or REGISTER
            if(clientAuths.split(":")[2].equalsIgnoreCase("LOGIN")){ // change 2 to 3
                // Check whether this credentials are in database collection Users
                check = Login(clientAuths);
                if(check){
                    break;
                }
            }
            else if (clientAuths.split(":")[3].equalsIgnoreCase("REGISTER")){
                // Create a document in Users

                Document user = new Document("_id", new ObjectId());
                user.append("username",clientAuths.split(":")[0])
                        .append("password",clientAuths.split(":")[1])
                        .append("collection",clientAuths.split(":")[0])
                        .append("phone",clientAuths.split(":")[2]);
                dbcol.insertOne(user);
                outToClient.writeBytes("User Added:Successfully"+'\n');

                // Create a collection in DCCN Database named after the Username
                CreateColl(clientAuths.split(":")[0]);

            }

            else if(clientAuths.split(":")[3].equalsIgnoreCase("USERNAMES")){
                // Get em the usernames
                ResponseMsg = getUsernames();
                outToClient.writeBytes(ResponseMsg+'\n');
            }

        }
        String[] arr = new String[1];
        arr[0]=ResponseMsg.split(":")[1];
        ServerCon server = new ServerCon(arr[0]);
        try{
            server.CreateAndListen(port);
        }
        catch(Exception e){
            System.out.println("Exception in Auth Server: "+e.getMessage());
            e.printStackTrace();
        }
        // Start main, Main starts Auth server, Auth server starts ServerCon
    }

    private boolean Login(String clientAuths) throws IOException {
//        String DBResp = inDb(DBGetConn(),clientAuths.split(":")[0],clientAuths.split(":")[1]);
        String DBResp = inDb(dbcol,clientAuths.split(":")[0],clientAuths.split(":")[1]);

        if(!DBResp.equalsIgnoreCase("NAN")){
            ResponseMsg = "ACCESS GRANTED:"; // Get Collection name here
            ResponseMsg += DBResp;
            //System.out.println("success");
            outToClient.writeBytes(ResponseMsg+'\n');
            return true;
        }
        else{
            ResponseMsg = "ACCESS DENIED:nan";
            //System.out.println("failed");
            outToClient.writeBytes(ResponseMsg+'\n');
            return false;
        }
    }

    private static MongoCollection<Document> DBGetConn(){
        String connection="";
        MongoClient mongoClient;
        MongoDatabase database;
        MongoCollection<Document> User_col;
        MongoClientSettings settings;

        ConnectionString connectionString = new ConnectionString(connection);
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("DCCN");
        User_col = database.getCollection("Users");

        return User_col;
    }

    private static void CreateColl(String name){
        String connection="";
        MongoClient mongoClient;
        MongoDatabase database;
        MongoClientSettings settings;
        MongoCollection<Document> New_col;

        ConnectionString connectionString = new ConnectionString(connection);
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("DCCN");
        New_col = database.getCollection(name);

        Document movie = new Document("_id", new ObjectId());
        movie.append("title","Sample Data")
                .append("rating",0.0)
                .append("runtime",0.0)
                .append("watched","Sample Data")
                .append("watch_date",new Date());
        New_col.insertOne(movie);
    }

    private static String inDb(MongoCollection<Document> dbcol, String Username, String Password){
        FindIterable<Document> users_found;
        users_found = dbcol.find().limit(500);

        for(Document i : users_found){
            //System.out.println("Vals: "+i.getString("username")+" "+i.getString("password"));
            //System.out.println("creds: "+Username+" "+Password);
            if(i.getString("username").equals(Username) && i.getString("password").equals(Password)){
                //System.out.println(i.getString("collection"));
                return i.getString("collection");
            }
        }

        return "NAN";
    }

    private String getUsernames(){
        FindIterable<Document> users_found;
        users_found = dbcol.find().limit(500);
        StringBuilder names = new StringBuilder();

        for(Document i : users_found){
            names.append(i.getString("username")).append(":");
        }
        return names.toString();
    }
}


