package aufgabe3;

import utils.Contact;
import utils.UserAlreadyExistsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread{
    private static BufferedReader br = null;
    private static InetAddress serverAddress;
    private static int serverPort;
    private static Socket serverConnect;
    private static int port;

    public ClientThread(int p) {
        port=p;
    }

    @Override
    public void run() {

        connectToServer();

        new Thread(() -> {
            try(BufferedReader r = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()))) {
                String line;
                do {
                    line = r.readLine();
                    System.out.println(line);
                } while (line != null && !line.equalsIgnoreCase("stop"));

            } catch (IOException e) {
                System.out.println("An error occurred while receiving message.");
            }
        }).start();

        try {
            PrintWriter w = new PrintWriter(serverConnect.getOutputStream(),true);
            String line;
            do {
                line = readString();
                w.println(line);
            } while (!line.equalsIgnoreCase("stop") && serverConnect.isConnected() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void connectToServer() {
        do {
            String[] splitData = readString().split(" ");
            if(splitData.length != 3 || !splitData[0].equals("connect")){
                System.out.println("The message was not well formatted. " +
                        "Needed format:" + "\nconnect <server ip> <server port>");
                continue;
            }

            if(!Contact.isValidIP(splitData[1])){
                System.out.println("No valid IP-Address: " + splitData[1]);
                continue;
            }

            int port = 0;
            try {
                port = Integer.parseInt(splitData[2]);
                if(port<1 || port>65535){
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Port number must be between 1 and 65535.");
                continue;
            }

            InetAddress ip;
            try {
                ip = InetAddress.getByName(splitData[1]);
            } catch (UnknownHostException e) {
                System.out.println("No valid IP-Address: " + splitData[1]);
                continue;
            }

            try {
                serverConnect = new Socket(ip,port);
            } catch (IOException e) {
                System.out.println("Error during connection. " + splitData[1]);
                continue;
            }
            serverAddress = ip;
            serverPort = port;
            break;

        } while (!currentThread().isInterrupted());
    }

    private static String readString () {
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

}
