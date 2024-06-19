package oxoo2a.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new HashMap<>();

    private static class ClientInfo {
        Socket socket;
        PrintWriter out;
        BufferedReader in;

        ClientInfo(Socket socket) {
            try {
                this.socket = socket;
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on IP " + InetAddress.getLocalHost().getHostAddress() + " on Port " + port + ".\nUse \"quit\" to exit program.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            ClientInfo clientInfo = new ClientInfo(clientSocket);
            String clientName = null;

            String line;
            while ((line = clientInfo.in.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                if (parts[0].equalsIgnoreCase("register") && parts.length == 2) {
                    clientName = parts[1];
                    if (clients.containsKey(clientName)) {
                        clientName = clientName + (int) (Math.random() * 1000); // add random number if name is taken
                    }
                    synchronized (clients) {
                        clients.put(clientName, clientInfo);
                    }
                    System.out.println(clientName + " registered.");
                } else if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    String recipient = parts[1];
                    String message = parts[2];
                    sendMessage(clientName, recipient, message);
                } else if (parts[0].equalsIgnoreCase("sendall")) {
                    sendToAllClients(clientName, line.substring(8)); // Send the message to all clients
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                    forwardPredefinedQuestion(clientName, parts[1], parts[2]);
                } else if (parts[0].equalsIgnoreCase("response") && parts.length == 3) {
                    String asker = parts[1];
                    String response = parts[2];
                    sendResponseToAsker(clientName, asker, response);
                } else {
                    clientInfo.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String sender, String recipient, String message) {
        synchronized (clients) {
            ClientInfo recipientInfo = clients.get(recipient);
            if (recipientInfo != null) {
                recipientInfo.out.println("Message from " + sender + ": " + message);
            } else {
                System.out.println("Client " + recipient + " not found.");
            }
        }
    }

    private static void sendToAllClients(String sender, String message) {
        synchronized (clients) {
            for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
                if (!entry.getKey().equals(sender)) {
                    entry.getValue().out.println("Message from " + sender + " to all: " + message);
                }
            }
        }
    }

    private static void forwardPredefinedQuestion(String asker, String recipient, String question) {
        synchronized (clients) {
            ClientInfo recipientInfo = clients.get(recipient);
            if (recipientInfo != null) {
                recipientInfo.out.println("ask " + asker + " " + question);
            } else {
                ClientInfo askerInfo = clients.get(asker);
                if (askerInfo != null) {
                    askerInfo.out.println("Client " + recipient + " not found.");
                }
            }
        }
    }

    private static void sendResponseToAsker(String responder, String asker, String response) {
        synchronized (clients) {
            ClientInfo askerInfo = clients.get(asker);
            if (askerInfo != null) {
                askerInfo.out.println("Response from " + responder + ": " + response);
            }
        }
    }
}
