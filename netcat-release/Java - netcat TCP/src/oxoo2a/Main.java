package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {

    private static HashMap<String, Socket> users = new HashMap<>();

    private static void fatal (String comment) {
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
            Client(args[0], port);
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server läuft auf Port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread(() -> serveClient(clientSocket));
            t.start();
        }
    }

    private static void serveClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("Geben Sie Ihren Benutzernamen ein:");
            String username = reader.readLine();
            synchronized (users) {
                users.put(username, clientSocket);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(username + ": " + line);
                if (line.startsWith("send")) {
                    String[] parts = line.split(" ", 3);
                    String targetUser = parts[1];
                    String message = parts[2];
                    sendMessage(targetUser, username + ": " + message);
                } else if (line.equalsIgnoreCase("stop")) {
                    break;
                }
            }
            clientSocket.close();
            synchronized (users) {
                users.remove(username);
            }
        } catch (IOException e) {
            System.out.println("Es gab eine IOException bei der Kommunikation mit dem Client ...");
        }
    }

    private static void sendMessage(String targetUser, String message) {
        Socket targetSocket;
        synchronized (users) {
            targetSocket = users.get(targetUser);
        }
        if (targetSocket != null) {
            try {
                PrintWriter targetWriter = new PrintWriter(targetSocket.getOutputStream(), true);
                targetWriter.println(message);
            } catch (IOException e) {
                System.out.println("Fehler beim Senden der Nachricht an " + targetUser);
            }
        } else {
            System.out.println("Benutzer " + targetUser + " nicht gefunden.");
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverSocket = new Socket(serverAddress, serverPort);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        BufferedReader serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        System.out.println(serverReader.readLine());
        String username = reader.readLine();
        writer.println(username);

        Thread serverListener = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = serverReader.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Verbindung zum Server verloren.");
            }
        });
        serverListener.start();

        String line;
        while ((line = reader.readLine()) != null) {
            writer.println(line);
            if (line.equalsIgnoreCase("stop")) {
                break;
            }
        }
        serverSocket.close();
    }
}
