package Server;

import Authenticator.AuthServer;
import DBUtils.DBConn;
import DBUtils.MoviesCollection;
import Models.Movies;
import com.diogonunes.jcolor.Attribute;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sun.tools.javac.Main;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;

import static com.diogonunes.jcolor.Ansi.colorize;

public class ServerCon {
    DatagramSocket socket = null;
    private FindIterable<Document> AllMovies;
    private ArrayList<Movies> MovieList;
    private String Colname;

    DatagramPacket incomingPacket;
    ByteArrayInputStream in;
    ObjectInputStream is;
    ByteArrayOutputStream outputStream;
    ObjectOutputStream os;

    public ServerCon(){
        DBConn.ConnectDB(this.Colname);
        AllMovies= MoviesCollection.allMovies();
        MovieList= MoviesCollection.FillMoviesNew(AllMovies);
    }

    public ServerCon(String colname){
        this.Colname = colname;
        System.out.println("colname in ServerCon :"+colname);
        DBConn.ConnectDB(colname);
        AllMovies= MoviesCollection.allMovies();
        MovieList= MoviesCollection.FillMoviesNew(AllMovies);
    }

    public void CreateAndListen(int port) throws Exception{
        System.out.println(colorize("[+] Server Started ...",Attribute.TEXT_COLOR(83)));
        socket = new DatagramSocket(port);

        while(true){
            byte[] incomingData = new byte[1024];
            incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);
            System.out.println(colorize("[+] Connection from ",Attribute.GREEN_TEXT())+colorize(incomingPacket.getAddress()+" "+incomingPacket.getPort(),Attribute.CYAN_TEXT()));

            byte[] data = incomingPacket.getData();
            in = new ByteArrayInputStream(data);
            is = new ObjectInputStream(in);

            String RecvMesg="";
            String RecvIcode="";
            ArrayList<Object> RecvData = new ArrayList<>();

            try{
                Packet cusPckt = (Packet) is.readObject();
                RecvIcode = cusPckt.Icode;
                RecvMesg = cusPckt.mesg;
                RecvData = cusPckt.Data;
                System.out.println("Icode: "+RecvIcode);
            }catch(Exception e){
                System.out.println("error");
                e.printStackTrace();
            }

            InetAddress IPAddress = incomingPacket.getAddress();
            int port1 = incomingPacket.getPort();
            Packet pckt = new Packet();

            try{
                if(RecvIcode.contains("VIEW")){
                    pckt.setMesg("Got the Movies");
                    pckt.setMovies(MoviesCollection.FillMoviesNew(MoviesCollection.allMovies()));
                    pckt.Icode="MOVIES";
                }
                else if(RecvIcode.contains("SEARCH")){
                    pckt.setMesg("Search Query");
                    pckt.setMovies(MoviesCollection.FilterMovies(AllMovies,RecvIcode.split(":")[1],RecvMesg));
                    pckt.Icode="S_MOVIES";
                }
                else if(RecvIcode.contains("ADD")){
                    pckt.setMesg("Adding data to Database");
                    MoviesCollection.InsertMovie(RecvData);
                    new ServerCon(this.Colname);
                    AllMovies= MoviesCollection.allMovies();
                    MovieList= MoviesCollection.FillMoviesNew(AllMovies);
                    pckt.setMovies(MovieList);
                    pckt.Icode="ADDED";
                }
                else if(RecvIcode.contains("DELETE")){
                    pckt.setMesg("Removing from Database");
//                    System.out.println("Received: "+RecvMesg);
//                    System.out.println("RecvIcode: "+RecvIcode+" RecvMesg:"+RecvMesg);
//                    System.out.println("Size returned: "+MoviesCollection.FilterMovies(AllMovies,RecvIcode,RecvMesg).size());

                    if(MoviesCollection.FilterMovies(AllMovies,"title",RecvMesg).size() == 0){
                        pckt.Icode="DELETED";
                        pckt.setMesg("NOT FOUND");
                    }
                    else {
                        MoviesCollection.deleteMovie("title", RecvMesg);
                        new ServerCon(this.Colname);
                        AllMovies = MoviesCollection.allMovies();
                        MovieList = MoviesCollection.FillMoviesNew(AllMovies);
                        pckt.setMovies(MovieList);
                        pckt.Icode = "DELETED";
                    }
                }
                else if(RecvIcode.contains("UPDATE")){
                    pckt.setMesg("Updating the Record");
                    if(MoviesCollection.FilterMovies(AllMovies,"title",RecvIcode.split(":")[1]).size() == 0){
                        pckt.Icode="UPDATE";
                        pckt.setMesg("NOT FOUND");
                    }
                    else {
                        System.out.println("Updated the movie");
                        MoviesCollection.UpdateMovie(RecvIcode.split(":")[1],RecvMesg);
                        new ServerCon(this.Colname);
                        AllMovies = MoviesCollection.allMovies();
                        MovieList = MoviesCollection.FillMoviesNew(AllMovies);
                        pckt.setMovies(MovieList);
                        pckt.Icode = "UPDATE";
                    }
                }
                else if(RecvIcode.equalsIgnoreCase("SEND_W")){
                    int APIKEY = Integer.parseInt(RecvMesg.split(":")[0]);
                    String query = RecvMesg.split(":")[1];
                    String username = Colname;
                    CraftMesg(username,query,APIKEY);

                    pckt.Icode="SEND_W";
                    pckt.setMesg("SEND SUCCESS");
                }
                else if(RecvIcode.contains("FIN")){
                    System.out.println("Fin Received");
                    // Send Fin packet back and exit to AuthServer Mode
                    ExitToAuth();
                }
                else{
                    pckt.setMesg("Didn`t Understand");
                }
            } catch(Exception e){
                System.out.println("Error in Recv: "+e.getMessage());
            }
            outputStream = new ByteArrayOutputStream();
            os = new ObjectOutputStream(outputStream);

            os.writeObject(pckt);
            byte[] data1 = outputStream.toByteArray();
            DatagramPacket replyPacket1 = new DatagramPacket(data1, data1.length, IPAddress, port1);
            socket.send(replyPacket1);
            new ServerCon(this.Colname);
        }
    }

    private void CraftMesg(String username, String Query, int apikey) {
        // Use moviesCollection to get list of movies (SEARCH)
        ArrayList<Movies> movieList = new ArrayList<>();
        movieList = MoviesCollection.FilterMovies(AllMovies,"watched",Query);

        // get phone number of username
        DBConn newdb = new DBConn();
        newdb.ConnectDB("Users");
        FindIterable<Document> users = newdb.getMovie_col().find();

        String phone = "";
        for(Document i : users){
            System.out.println("uname: "+username+" Users: "+i.getString("username")+" phone: "+i.getString("phone"));
            if(username.equals(i.getString("username"))){
                phone = i.getString("phone");
            }
        }

        SendMesg(movieList,phone,apikey);
    }

    private void SendMesg(ArrayList<Movies> movieList, String phone,int apikey) {
        // CallMeBot Api Call here
        // -------------------------- Maybe use Twilio
        // Turn arraylist to string
        StringBuilder mesg= new StringBuilder();
        mesg.append("Movies%0A");
        for(Movies i : movieList){
            mesg.append("%0A[ ").append(i.getTitle()).append(" ]%0A");
        }
        System.out.println(mesg.toString());
        String BASE_URL = "https://api.callmebot.com/whatsapp.php?phone="+phone+"&text="+mesg.toString()+"&apikey="+String.valueOf(apikey);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            //System.out.println("Resp: "+response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ExitToAuth() throws IOException {
        socket.close();
        System.exit(1);
    }
}

