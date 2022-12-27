/*
	[+] Abdullah Mohammad Shafique --- FA19-BCS-007
	[+] Mohammad Tayyab Akbar --- FA19-BCS-039
	[+] Class: BCS-4A
*/

package DBUtils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DBConn {
    private static String connection="";
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    public static MongoCollection<Document> Movie_col;
    private static MongoClientSettings settings;

    public static void ConnectDB(String colname) {
        ConnectionString connectionString = new ConnectionString(connection);
        settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("DCCN");
        Movie_col = database.getCollection(colname);
    }

    public static MongoCollection<Document> getMovie_col() {
        return Movie_col;
    }
}
