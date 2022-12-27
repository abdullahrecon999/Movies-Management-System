package Client;

import java.net.*;
import java.io.*;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;


public class User {
    private String username;
    private String pass;
    private String auth;
    private static String Response;
    public static Scanner inp;

    private static Socket clientSocket;

    public static void main(String[] args) throws IOException {
        while(true) {
            try {
                System.out.println(colorize("\n+-------------------------------------------------+",TEXT_COLOR(13)));
                System.out.println(colorize("|",TEXT_COLOR(13))+colorize(" Welcome to The Personal Movie Management System "
                        ,TEXT_COLOR(84))+colorize("|",TEXT_COLOR(13)));
                System.out.println(colorize("+-------------------------------------------------+\n",TEXT_COLOR(13)));
                System.out.println(colorize("[1]",BOLD(),BRIGHT_YELLOW_TEXT(),RED_BACK())+" Login to Registered Account");
                System.out.println(colorize("[2]",BOLD(),BRIGHT_YELLOW_TEXT(),RED_BACK())+" Register a new Account");
                System.out.print("/> ");
                inp = new Scanner(System.in);
                int opt = inp.nextInt();

                switch (opt){
                    case 1:
                        new User();
                        break;
                    case 2:
                        Register();
                        break;
                    default:
                        System.out.println("[-] Please choose from options");
                        continue;
                }
                break;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
//        new User();
    }

    private static void Register() throws IOException {

        // Get usernames in an arrayList
        // Authsed("mesg/data")
        String serverResp = SendToServer(":::USERNAMES");
        ArrayList<String> Nameslist = new ArrayList<String>(Arrays.asList(serverResp.split(":")));

        while(true) {
            try {
                System.out.println("\n---------- REGISTRATION SYSTEM ----------");
                // Verify for the Duplicate username from server
                // Send the AUTH server a special packet for Registration
                // Get Username, Password and Phone number (Whatsapp)
                inp = new Scanner(System.in);
                System.out.print("Enter Valid Username (no Special chars, 4 chars and 4 numbers): ");
                String uname = inp.nextLine();
                System.out.print("Enter Valid Password (Min 8 chars, Atleast 1 upper case, lower case char and number): ");
                String pass = inp.nextLine();
                System.out.print("Enter Valid phone number (+xxx0000000000): ");
                String phone = inp.nextLine();

                if(!uname.matches("[A-Za-z]{4,}[0-9]{1,}")){
                    throw new Exception("Incorrect Username");
                }

                if(!pass.matches("(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}")){
                    throw new Exception("Incorrect Password");
                }

                if(!phone.matches("\\+[0-9]{2,3}[0-9]{9}")){
                    throw new Exception("Incorrect Phone number");
                }

                // Check if the username is already taken
                // First retrieve all the available username in an arraylist at the start of user-Register()

                for(String i : Nameslist){
                    if(uname.equals(i)){
                        throw new Exception("Username Already taken!");
                    }
                }

                // Send the Registration Data now
                SendToServer(uname+":"+pass+":"+phone+":"+"REGISTER");

                break;
            } catch(Exception e){
                System.out.println("[-] "+e.getMessage());
            }
            System.out.println("[+] Registration Successful");
        }
    }

    User() throws IOException {
        do {
          inp = new Scanner(System.in);
          Take_input();
          this.auth = AuthSend();
        }while(this.auth.split(":")[0].equalsIgnoreCase("Access Denied"));
        // if authenticated, then start clientUDP with collection name
        System.out.println(colorize("________ CLIENT STARTING ________",TEXT_COLOR(190)));
        System.out.print("> ");
        try {
            for(int i=0;i<6;i++){
                TimeUnit.SECONDS.sleep(1);
                System.out.print("-");
            }
            ClientUDP client = new ClientUDP();
            client.CreateAndListen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Take_input(){
        while(true) {
            try {
                System.out.println(colorize("-------- LOGIN SYSTEM --------",TEXT_COLOR(118)));
                System.out.println("Enter Username: ");
                this.username = inp.next();
                System.out.println("Enter Password: ");
                this.pass = inp.next();
                break;
            } catch (Exception e) {
                System.out.println("[-] Incorrect data entered");
            }
        }
    }

    private String AuthSend() throws IOException { // Modify it to be more flexible
        try{
            clientSocket = new Socket("localhost", 1234);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(username+":"+pass+":"+"LOGIN"+ '\n');
            Response = inFromServer.readLine();
        }catch(Exception e){
            System.out.println("[-] Server Is not Responding...(exiting)");
        }

        if(Response.split(":")[0].equalsIgnoreCase("Access Granted")){
            clientSocket.close();
            return Response;
        }
        else if(Response.split(":")[0].equalsIgnoreCase("Access Denied")){
            System.out.println("INCORRECT CREDENTIALS");
        }

        clientSocket.close();
        return Response;
    }

    private static String SendToServer(String data) throws IOException { // Modify it to be more flexible
        try{
            clientSocket = new Socket("localhost", 1234);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(data+'\n');
            Response = inFromServer.readLine();
            clientSocket.close();
        }catch(Exception e){
            System.out.println("[-] Server Is not Responding...(exiting)");
        }
        return Response;
    }
}
