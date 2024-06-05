//fehlerhaft



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class nc_tdp {

    private static final Map<String, Socket> registeredClients = new HashMap<>();

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            fatal("Usage: \"<server> -l <port>\"");
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        Thread clientListener = new Thread(() -> listenForClients(serverSocket));
        clientListener.start();
        Thread userInput = new Thread(() -> readUserInput());
        userInput.start();
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void listenForClients(ServerSocket serverSocket){
        try {
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread(() -> handleClient(clientSocket));
            t.start();
        } catch (IOException e){
            System.out.println("Error accepting client connections: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket){
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter w = new PrintWriter(clientSocket.getOutputStream(), true);
            String line;
            while ((line = r.readLine()) != null){
                if (line.startsWith("register")){
                    String[] parts = line.split(" ");
                    String name = parts[1];
                    registeredClients.put(name, clientSocket);
                    System.out.println("Client " + name + " registered.");
                } else if (line.startsWith("send")) {
                    String[] parts = line.split(" ", 3);
                    String targetName = parts[1];
                    String message = parts[2];
                    if (registeredClients.containsKey(targetName)){
                        Socket targetSocket = registeredClients.get(targetName);
                        PrintWriter targetWriter = new PrintWriter(targetSocket.getOutputStream(),true);
                        targetWriter.println(message);
                    } else {
                        System.out.println("Unknown client: " + targetName);
                    }
                }
            }
        } catch (IOException e){
            System.out.println("Error accepting client connections: " + e.getMessage());
        }
    }


    // ************************************************************************
    // Client
    // ************************************************************************
    private static void readUserInput() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String input = br.readLine();
                if (input.startsWith("connect")) {
                    String[] parts = input.split(" ");
                    if (parts.length != 4) {
                        System.out.println("Invalid command. Usage: connect <serverHost> <serverPort> <name>");
                    } else {
                        String serverHost = parts[1];
                        int serverPort = Integer.parseInt(parts[2]);
                        String name = parts[3];
                        connectToServer(serverHost, serverPort, name);
                    }
                } else if (input.startsWith("send")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length != 3) {
                        System.out.println("Invalid command. Usage: send <targetName> <message>");
                    } else {
                        String targetName = parts[1];
                        String message = parts[2];
                        sendMessageToServer(targetName, message);
                    }
                } else {
                    System.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user input: " + e.getMessage());
        }
    }

    private static void connectToServer(String serverHost, int serverPort, String name) {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println("register " + name);
            System.out.println("Connected to server as " + name);
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void sendMessageToServer(String targetName, String message) {
        try {
            Socket targetSocket = registeredClients.get(targetName);
            if (targetSocket != null) {
                PrintWriter writer = new PrintWriter(targetSocket.getOutputStream(), true);
                writer.println("send " + message);
            } else {
                System.out.println("Unknown client: " + targetName);
            }
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }
}
