package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static class ClientInfo {
        String name;
        Socket socket;
        PrintWriter writer;

        ClientInfo(String name, Socket socket, PrintWriter writer) {
            this.name = name;
            this.socket = socket;
            this.writer = writer;
        }
    }

    private static Map<String, ClientInfo> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            server(port);
        else
            client(args[0], port);
    }

    private static void server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread clientHandler = new Thread(() -> handleClient(clientSocket));
            clientHandler.start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            String line;
            String clientName = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("register")) {
                    String[] parts = line.split(" ");
                    clientName = parts[1];
                    clients.put(clientName, new ClientInfo(clientName, clientSocket, writer));
                    System.out.println(clientName + " registered.");
                    writer.println("ack " + clientName);
                } else if (line.startsWith("send")) {
                    String[] parts = line.split(" ", 3);
                    String recipientName = parts[1];
                    String message = parts[2];
                    ClientInfo recipient = clients.get(recipientName);
                    if (recipient != null) {
                        recipient.writer.println(clientName + ": " + message);
                        System.out.println("Message from " + clientName + " to " + recipientName + ": " + message);
                    } else {
                        writer.println("Recipient " + recipientName + " not found.");
                    }
                } else if (line.equalsIgnoreCase("stop")) {
                    break;
                }
            }

            if (clientName != null) {
                clients.remove(clientName);
                System.out.println(clientName + " disconnected.");
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void client(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverSocket = new Socket(serverAddress, serverPort);
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter your name for registration:");
        String name = consoleReader.readLine();
        writer.println("register " + name);

        String ack = reader.readLine();
        System.out.println("Server: " + ack);

        Thread listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenerThread.start();

        String line;
        while (true) {
            line = consoleReader.readLine();
            if (line.equalsIgnoreCase("stop")) {
                writer.println("stop");
                break;
            }
            writer.println(line);
        }

        serverSocket.close();
    }
}
