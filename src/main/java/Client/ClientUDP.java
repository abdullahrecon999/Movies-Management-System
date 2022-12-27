package Client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

import Models.Movies;
import Server.Packet;
import Utils.ObjSerD;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class ClientUDP {
    DatagramSocket socket;
    byte[] incomingData;
    Packet CustomPacket;
    DatagramPacket sendPacket;
    DatagramPacket incomingPacket;
    InetAddress ipaddr;
    private ArrayList<Movies> MovieList;

    public static void main(String[] arv) throws Exception{
        ClientUDP client = new ClientUDP();
        client.CreateAndListen();
    }

    ClientUDP() throws Exception {}

    public void CreateAndListen() throws Exception{
        socket = new DatagramSocket();
        ipaddr = InetAddress.getByName("localhost");

        while(true){
            DisplayMenu();
        }
    }

    private void DisplayData(ArrayList<Movies> Datalist){
        System.out.println(colorize("\n\t\t\t RESULT DISPLAY ",BLUE_BACK(),RED_TEXT(),BOLD()));
        for(int i=0;i<100;i++){
            System.out.print(colorize("=",TEXT_COLOR(85)));
        }
        System.out.println();

        System.out.printf(colorize("%-25s%-10s%-10s%-30s%-25s\n",TEXT_COLOR(11)), "Movie Title", "Rating", "Watched", "WatchDate", "Runtime");
        for(int i=0;i<100;i++){
            System.out.print("_");
        }
        System.out.println();
        for (int i=0;i<Datalist.size();i++){
            System.out.printf("%-25s%-10s%-10s%-30s%-25s\n", Datalist.get(i).getTitle(), Datalist.get(i).getRating(), Datalist.get(i).isWatched(), Datalist.get(i).getWatchedDate(), Datalist.get(i).getRuntime());
        }
        if(Datalist.size()==0){
            System.out.println(colorize("[-] No Record Found",RED_TEXT()));
        }
        System.out.println("\nPress \"ENTER\" to go back to Menu...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        DisplayMenu();
    }

    private void DisplayMenu(){
        while(true) {
            try {
                System.out.println(colorize("\n\t\t\t MENU DISPLAY ",TEXT_COLOR(13)));
                for (int i = 0; i < 85; i++) {
                    System.out.print("=");
                }
                System.out.println();

                Scanner OptionInput = new Scanner(System.in);
                System.out.println("Select the Appropriate Option:");
                System.out.println("[1] View Movies Record");
                System.out.println("[2] Search Movies");
                System.out.println("[3] Add a Movie");
                System.out.println("[4] Delete a Movie");
                System.out.println("[5] Update Record");
                System.out.println("[6] Send List to Whatsapp");
                System.out.println("[7] Exit");
                System.out.print(":> ");
                int opt = OptionInput.nextInt();

                switch(opt) {
                    case 1:
                        System.out.println("\n[View Movies Record] Selected");
                        ViewMovies();
                        break;
                    case 2:
                        System.out.println("\n[Search Movies] Selected");
                        SearchMovieMenu();
                        break;
                    case 3:
                        System.out.println("\n[Add a Movie] Selected");
                        AddMovieMenu();
                        break;
                    case 4:
                        System.out.println("\n[Delete a Movie] Selected");
                        DeleteMovieMenu();
                        break;
                    case 5:
                        System.out.println("\n[Update Record] Selected");
                        UpdateRecordMenu();
                        break;
                    case 6:
                        System.out.println("\n[Send To Whatsapp] Selected");
                        SendList();
                        break;
                    case 7:
                        Packet pckt = new Packet("FIN","");
                        SendRecvData(pckt);
                        break;
                    default:
                        System.out.println("[-] Please choose from Option!");
                        break;
                }
                return;
            } catch(InputMismatchException e){
                System.out.println("[-] Input Error");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void SendList() {
        while(true) {
            try {
                Scanner inp = new Scanner(System.in);
                System.out.println("\n---------- Send List to Whatsapp ----------");
                System.out.println("[*] INSTRUCTIONS");
                System.out.println("- Add the phone number +34 644 76 66 43 into your Phone Contacts");
                System.out.println("- Send this message \"I allow callmebot to send me messages\" to Saved whatsapp contact");
                System.out.println("- Wait upto 2 minutes for the API key to be generated");

                System.out.print("\nEnter The API KEY: ");
                int APIKEY = inp.nextInt();
                inp.nextLine();
                System.out.print("Enter The Status of movies to be sent (Pending/True/Yes/No/False): ");
                String stat = inp.nextLine();
                // Send this to server
                Packet pckt = new Packet("SEND_W",String.valueOf(APIKEY)+":"+stat);
                SendRecvData(pckt);
                break;
            }catch(Exception e){
                System.out.println("[-] Input Error");
            }
        }
    }

    private void DeleteMovieMenu() {
        while(true){
            try{
                Scanner OptionInput = new Scanner(System.in);
                System.out.println(colorize("\n\t\t UPDATE RECORD",TEXT_COLOR(13)));
                for(int i=0;i<50;i++){
                    System.out.print("_");
                }
                System.out.println("\nenter Movie Title:");
                System.out.print(":>");
                String SearchTitle = OptionInput.nextLine();
                DeleteMovie("title",SearchTitle);

            }catch(InputMismatchException | IOException | ClassNotFoundException e){
                System.out.println("[-] Input Error");
            }
        }
    }

    private void UpdateRecordMenu() {
        while(true){
            try{
                Scanner OptionInput = new Scanner(System.in);
                System.out.println(colorize("\n\t\t UPDATE RECORD",TEXT_COLOR(13)));
                for(int i=0;i<50;i++){
                    System.out.print("_");
                }
                System.out.println("\nTitle:");
                System.out.print(":>");
                String SearchTitle = OptionInput.nextLine();
                System.out.println("Enter New Status:");
                System.out.print(":>");
                String Status = OptionInput.nextLine();

                UpdateMovie(SearchTitle, Status);

            }catch(InputMismatchException | IOException | ClassNotFoundException e){
                System.out.println("[-] Input Error");
            }
        }
    }

    private void UpdateMovie(String SearchTitle, String Status) throws IOException, ClassNotFoundException {
        Packet pckt = new Packet("UPDATE:"+SearchTitle,Status);
        SendRecvData(pckt);
    }

    private void DeleteMovie(String title, String Title) throws IOException, ClassNotFoundException {
        Packet pckt = new Packet("DELETE",Title);
        SendRecvData(pckt);
    }

    private void ViewMovies() throws IOException, ClassNotFoundException {
        Packet pckt = new Packet("VIEW","");
        SendRecvData(pckt);
    }

    private void SearchMovieMenu(){
        while(true){
            try{
                Scanner OptionInput = new Scanner(System.in);
                System.out.println(colorize("\n\t\t SEARCH RECORD",TEXT_COLOR(13)));
                for(int i=0;i<50;i++){
                    System.out.print("_");
                }
                System.out.println("\nSearch By: \n[1] Title\n[2] Watched");
                System.out.print(":>");
                int opt = OptionInput.nextInt();

                switch (opt){
                    case 1:
                        System.out.print("Enter Title: ");
                        String SearchTitle = OptionInput.next();
                        SearchMovie("title",SearchTitle);
                        break;
                    case 2:
                        System.out.print("Enter Required Watched Status: ");
                        String WatchedStat = OptionInput.next();
                        SearchMovie("watched",WatchedStat);
                        break;
                    default:
                        System.out.println("[-] Please choose from Option!");
                        break;
                }

            }catch(InputMismatchException | IOException | ClassNotFoundException e){
                System.out.println("[-] Input Error");
            }
        }
    }

    private void SearchMovie(String index, String val) throws IOException, ClassNotFoundException {
        Packet pckt = new Packet("SEARCH:"+index,val);
        SendRecvData(pckt);
    }

    private void AddMovieMenu(){
        while(true){
            try{
                Scanner input = new Scanner(System.in);
                System.out.println(colorize("\n\t\t Enter a new Record",TEXT_COLOR(13)));
                for(int i=0;i<50;i++){
                    System.out.print("_");
                }
                ArrayList<Object> RecordData = new ArrayList<>();
                System.out.print("\nEnter Title: ");
                String title= input.nextLine();
                if(title.isEmpty() || title.isBlank()){
                    throw new Exception("Title Cannot be Empty");
                }
                RecordData.add(title);
                System.out.print("Set Rating: ");
                RecordData.add(input.nextDouble());
                System.out.print("Enter Runtime: ");
                RecordData.add(input.nextDouble());
                System.out.print("Watched?: ");
                RecordData.add(input.next());
                System.out.print("Date Watched [Today for current date]: \n");
                RecordData.add(new Date());

                insertMovie(RecordData);

                for(int i=0;i<RecordData.size();i++){
                    System.out.println("Data: "+RecordData.get(i).toString());
                }

                break;
            }catch(InputMismatchException e){
                System.out.println("[-] Input Error");
            } catch (Exception e) {
                System.out.println("[-] "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void insertMovie(ArrayList<Object> RecordData) throws IOException, ClassNotFoundException {
        Packet pckt = new Packet("ADD",RecordData);
        SendRecvData(pckt);
    }

    private void SendRecvData(Packet pckt) throws IOException, ClassNotFoundException {

        incomingData = new byte[99999];
        byte[] data = ObjSerD.Serialize(pckt);
        sendPacket = new DatagramPacket(data, data.length, ipaddr, 3191);
        socket.send(sendPacket);
        System.out.println("["+pckt.Icode+"] Packet Sent");

        if(pckt.Icode.equalsIgnoreCase("fin")){
            System.out.println("Bye");
            socket.close();
            System.exit(1);
        }

        incomingPacket = new DatagramPacket(incomingData, incomingData.length);
        socket.receive(incomingPacket);
        CustomPacket = (Packet) ObjSerD.DeSerialize(incomingPacket.getData());

        String icode = CustomPacket.Icode;
        System.out.println("Response from server:" + CustomPacket.mesg);

        if(icode.equalsIgnoreCase("MOVIES")){
            MovieList = CustomPacket.getMovies();
            DisplayData(MovieList);
        }
        else if(icode.equalsIgnoreCase("S_MOVIES")){
            MovieList = CustomPacket.getMovies();
            DisplayData(MovieList);
        }
        else if(icode.equalsIgnoreCase("ADDED")){
            MovieList = CustomPacket.getMovies();
            DisplayData(MovieList);
        }
        else if(icode.equalsIgnoreCase("DELETED")){
            if(CustomPacket.mesg.equalsIgnoreCase("NOT FOUND")){
                System.out.println("Movie Not Found");
                DisplayMenu();
            }
            else {
                MovieList = CustomPacket.getMovies();
                DisplayData(MovieList);
            }
        }
        else if(icode.equalsIgnoreCase("UPDATE")){
            if(CustomPacket.mesg.equalsIgnoreCase("NOT FOUND")){
                System.out.println("Movie Not Found");
                DisplayMenu();
            }
            else {
                MovieList = CustomPacket.getMovies();
                DisplayData(MovieList);
            }
        }
        else if(icode.equalsIgnoreCase("SEND_W")){
            System.out.println("Server Reply: "+CustomPacket.mesg);
        }
        else{
            System.out.println("ICODE: Error");
        }
    }
}
