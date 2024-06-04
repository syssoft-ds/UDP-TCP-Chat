import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static Map<String, ClientInfo> clients = new HashMap<>();

    private static class ClientInfo {
        String name;
        InetAddress ip;
        int port;

        ClientInfo(String name, InetAddress ip, int port) {
            this.name = name;
            this.ip = ip;
            this.port = port;
        }
    }

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            fatal("Usage: java Main <server|client> [options]");
        }

        String mode = args[0];
        if (mode.equalsIgnoreCase("server")) {
            if (args.length != 2) {
                fatal("Usage: java Main server <serverPort>");
            }
            int serverPort = Integer.parseInt(args[1]);
            startServer(serverPort);
        } else if (mode.equalsIgnoreCase("client")) {
            if (args.length != 4) {
                fatal("Usage: java Main client <clientName> <clientIp> <clientPort>");
            }
            String clientName = args[1];
            String clientIp = args[2];
            int clientPort = Integer.parseInt(args[3]);
            startClient(clientName, clientIp, clientPort);
        } else {
            fatal("Unknown mode: " + mode);
        }
    }

    // ************************************************************************
    // SERVER
    // ************************************************************************
    private static void startServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String registrationMessage = in.readLine();
            String[] registrationParts = registrationMessage.split(" ");
            String clientName = registrationParts[0];
            InetAddress clientIp = InetAddress.getByName(registrationParts[1]);
            int clientPort = Integer.parseInt(registrationParts[2]);

            synchronized (clients) {
                clients.put(clientName, new ClientInfo(clientName, clientIp, clientPort));
            }

            System.out.println(clientName + " registered with IP " + clientIp + " and port " + clientPort);
            out.println("Registration successful");

            String message;
            while ((message = in.readLine()) != null) {
                String[] messageParts = message.split(" ", 2);
                String recipientName = messageParts[0];
                String messageText = messageParts[1];

                ClientInfo recipientInfo;
                synchronized (clients) {
                    recipientInfo = clients.get(recipientName);
                }

                if (recipientInfo != null) {
                    sendMessage(recipientInfo, clientName + ": " + messageText);
                } else {
                    out.println("User " + recipientName + " not found.");
                }
            }

            clientSocket.close();
            synchronized (clients) {
                clients.remove(clientName);
            }
            System.out.println(clientName + " disconnected.");

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private static void sendMessage(ClientInfo recipientInfo, String message) {
        try (Socket socket = new Socket(recipientInfo.ip, recipientInfo.port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            System.out.println("Error sending message to " + recipientInfo.name + ": " + e.getMessage());
        }
    }

    // ************************************************************************
    // CLIENT
    // ************************************************************************
    private static void startClient(String clientName, String clientIp, int clientPort) throws IOException {
        InetAddress clientAddress = InetAddress.getByName(clientIp);

        // Start listening for messages
        new Thread(() -> listenForMessages(clientAddress, clientPort)).start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter server IP: ");
        String serverIp = stdIn.readLine();
        System.out.print("Enter server port: ");
        int serverPort = Integer.parseInt(stdIn.readLine());

        try (Socket serverSocket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {

            out.println(clientName + " " + clientIp + " " + clientPort);
            System.out.println(in.readLine());

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("bye")) {
                    break;
                } else if (userInput.startsWith("send ")) {
                    String[] parts = userInput.split(" ", 3);
                    if (parts.length == 3) {
                        String recipientName = parts[1];
                        String message = parts[2];
                        out.println(recipientName + " " + message);
                    } else {
                        System.out.println("Usage: send <recipientName> <message>");
                    }
                } else {
                    System.out.println("Unknown command. Type 'send <recipientName> <message>' to send a message.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void listenForMessages(InetAddress clientAddress, int clientPort) {
        try (ServerSocket listener = new ServerSocket(clientPort, 0, clientAddress)) {
            while (true) {
                try (Socket socket = listener.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error listening for messages: " + e.getMessage());
        }
    }
}
