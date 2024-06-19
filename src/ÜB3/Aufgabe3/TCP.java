package ÜB3.Aufgabe3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCP {

    private static Map<String, ClientHandler> clients = new HashMap<>();

    private static void fatal(String comment) {
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
        System.out.println("Server started on port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread(() -> handleClient(clientSocket));
            t.start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Registration
            writer.println("Enter your name: ");
            String name = reader.readLine();
            synchronized (clients) {
                clients.put(name, new ClientHandler(clientSocket, reader, writer));
            }
            writer.println("Registered as " + name);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("send ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 3) {
                        writer.println("Invalid command. Usage: send <name> <message>");
                        continue;
                    }
                    String targetName = parts[1];
                    String message = parts[2];
                    sendMessage(name, targetName, message);
                } else if (line.equalsIgnoreCase("stop")) {
                    break;
                } else {
                    writer.println("Unknown command: " + line);
                }
            }

            synchronized (clients) {
                clients.remove(name);
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String senderName, String targetName, String message) {
        synchronized (clients) {
            ClientHandler targetClient = clients.get(targetName);
            if (targetClient != null) {
                targetClient.writer.println(senderName + ": " + message);
            } else {
                ClientHandler senderClient = clients.get(senderName);
                if (senderClient != null) {
                    senderClient.writer.println("User " + targetName + " not found.");
                }
            }
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverSocket = new Socket(serverAddress, serverPort);
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String line;

        System.out.println(reader.readLine()); // Enter your name:
        writer.println(consoleReader.readLine()); // Send name to server

        ClientListener clientListener = new ClientListener(reader);
        Thread listenerThread = new Thread(clientListener);
        listenerThread.start();

        while ((line = consoleReader.readLine()) != null) {
            writer.println(line);
            if (line.equalsIgnoreCase("stop")) {
                break;
            }
        }

        serverSocket.close();
    }

    private static class ClientHandler {
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        ClientHandler(Socket socket, BufferedReader reader, PrintWriter writer) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
        }
    }

    private static class ClientListener implements Runnable {
        private BufferedReader reader;

        ClientListener(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}