package ÜB4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    private static class ClientInfo {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        boolean isAlive = true;

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

    private static void sendAll(String sender, String message) {
        synchronized (clients) {
            for (String recipient : clients.keySet()) {
                sendMessage(sender, recipient, message);
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            ClientInfo clientInfo = new ClientInfo(clientSocket);
            String clientName = null;

            String line;
            while ((line = clientInfo.in.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts[0].equalsIgnoreCase("register") && parts.length == 2) {
                    clientName = parts[1];
                    if (clients.containsKey(clientName)) {
                        clientName = clientName + (int) (Math.random() * 1000); // add random number if name is taken
                    }
                    synchronized (clients) {
                        clients.put(clientName, clientInfo);
                    }
                    System.out.println(clientName + " registered.");
                } else if (parts[0].equalsIgnoreCase("send")) {
                    if (parts[1].startsWith("all ")) {
                        String message = parts[1].substring(4);
                        sendAll(clientName, message);
                    } else {
                        String[] messageParts = parts[1].split(" ", 2);
                        sendMessage(clientName, messageParts[0], messageParts[1]);
                    }
                } else if (line.equalsIgnoreCase("getclientlist")) {
                    sendPingToAllClients(clientName);
                } else if (line.equalsIgnoreCase("pong")) {
                    if (clientName != null) {
                        synchronized (clients) {
                            ClientInfo ci = clients.get(clientName);
                            if (ci != null) {
                                ci.isAlive = true;
                            }
                        }
                    }
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

    private static void sendPingToAllClients(String requestingClient) {
        synchronized (clients) {
            for (ClientInfo clientInfo : clients.values()) {
                clientInfo.isAlive = false;  // reset the alive status
                clientInfo.out.println("ping");
            }
        }

        try {
            // Wait for a specified time to receive pongs
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Remove clients that did not respond with pong
        synchronized (clients) {
            clients.entrySet().removeIf(entry -> !entry.getValue().isAlive);
        }

        // Send the updated client list to the requesting client
        ClientInfo requestingClientInfo = clients.get(requestingClient);
        if (requestingClientInfo != null) {
            requestingClientInfo.out.println("Active clients: " + clients.keySet());
        }
    }
}
