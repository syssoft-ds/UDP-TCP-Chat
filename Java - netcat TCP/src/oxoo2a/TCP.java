package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class TCP {

    private static ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();

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
            // Add the client connection to the map
            clients.put(client.getInetAddress().toString() + ":" + client.getPort(), client);
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient ( Socket clientConnection ) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;
            do {
                line = r.readLine();
                System.out.println(clientConnection.getInetAddress() + ":" + clientConnection.getPort() + ": " + line);
                if (line.startsWith("send ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length != 3) {
                        System.out.println("Invalid message format");
                    } else {
                        System.out.println(clientConnection.getPort() + " wants to say " + parts[2] + " to " + parts[1]);
                        System.out.println("Clients: " + clients.keySet());
                        if (clients.containsKey(parts[1])) {
                            try {
                                PrintWriter w = new PrintWriter(clients.get(parts[1]).getOutputStream(), true);
                                System.out.println(clientConnection.getPort() + " sent to " + parts[1] + ": " + parts[2]);
                                String message = clientConnection.getPort() + ": " + parts[2];
                                w.println(message);
                            } catch (IOException e) {
                                System.out.println("There was an IOException while sending data ...");
                                System.exit(-1);
                            }
                        } else {
                            System.out.println("Unknown client: " + parts[1]);
                        }
                    }
                }
            } while (!line.equalsIgnoreCase("stop"));
            clientConnection.close();
            // Remove the client connection from the map
            clients.remove(String.valueOf(clientConnection.getPort()));
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

    // Create a new thread to listen for and process incoming messages
    Thread t = new Thread(() -> {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()));
            String incomingMessage;
            while ((incomingMessage = r.readLine()) != null) {
                System.out.println();
                System.out.println(incomingMessage);
            }
        } catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    });
    t.start();

    String line;
    do {
        line = readString();
        w.println(line);
    } while (!line.equalsIgnoreCase("stop"));
    serverConnect.close();
}

    private static String readString () {
        boolean again = false;
        String input = null;
        do {
            System.out.print("Input: ");
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
}
