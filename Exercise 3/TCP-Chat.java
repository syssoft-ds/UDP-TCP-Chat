package oxoo2a;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {

    static HashMap<String, ClientData> users = new HashMap<>();

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            Server(port);
        else
            Client(args[0],port);
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server ( int port ) throws IOException {
        ServerSocket s = new ServerSocket(port);
        while (true) {
            Socket client = s.accept();
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient ( Socket clientConnection ) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            PrintWriter w = new PrintWriter(clientConnection.getOutputStream(),true);
            String line;
            String name = "NONE";
            boolean registered = false;
            do {
                line = r.readLine();
                System.out.println(line);
                if (line.startsWith("register")){
                    String[] parts = line.split(" ");
                    name = parts[1];
                    users.put(name,new ClientData(clientConnection,w));
                    System.out.println("registered: " + name);
                    registered = true;
                    w.println("successfully registered ");
                }
                if (line.startsWith("send") && registered){
                    String[] parts = line.split(" ");
                    String user = parts[1];
                    String message = line.substring(parts[0].length() + parts[1].length() + 2);
                    ClientData user_info = users.get(user);
                    user_info.writer.println("<"+ name +"> "+ message);
                    w.println("message sent");
                }
            } while (!line.equalsIgnoreCase("stop"));
            users.remove(name);
            clientConnection.close();
        }
        catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client ( String serverHost, int serverPort ) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverConnect = new Socket(serverAddress,serverPort);
        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(),true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()));

        Thread receiveThread = new Thread(() -> {
            try {
                String receivedMessage;
                while ((receivedMessage = reader.readLine()) != null) {
                    System.out.println(receivedMessage);
                }
            } catch (IOException e) {
                System.out.println("Error occurred while receiving message: " + e.getMessage());
            }
        });
        receiveThread.start();

        String line;
        do {
            line = readString();
            w.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        receiveThread.interrupt();
        serverConnect.close();
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

    private static BufferedReader br = null;


    private static class ClientData {
        Socket socket;
        PrintWriter writer;

        ClientData(Socket socket, PrintWriter writer){
            this.socket = socket;
            this.writer = writer;
        }
    }
}

