import Authenticator.AuthServer;
import Server.ServerCon;

public class Main {

    private static int port = 3191;

    public static void main(String[] argv) {
        try{
            // When User Exits, The server returns to AuthServer mode

            String[] portArr = new String[1];
            portArr[0] = Integer.toString(port);
            AuthServer authserver = new AuthServer(port);
            //AuthServer.main(portArr);
        }
        catch(Exception e){
            System.out.println("Exception in Main: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
