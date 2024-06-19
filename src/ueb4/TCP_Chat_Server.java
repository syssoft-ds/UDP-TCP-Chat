package ueb4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static Set<String> noPong = new HashSet<>();

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
                    if (recipient.equalsIgnoreCase("all")) {
                        Set<String> names = clients.keySet();
                        for (String n : names) {
                            if (!n.equals(clientName)) {
                                sendMessage(clientName, n, message);
                            }
                        }
                    } else {
                        sendMessage(clientName, recipient, message);
                    }
                } else if (parts[0].equalsIgnoreCase("getclientlist")) {
                    Set<String> names = new HashSet<>(clients.keySet());
                    names.remove(clientName);
                    synchronized (noPong) {
                        noPong = new HashSet<>(names);
                    }
                    for (String n : names) {
                        ClientInfo nInfo = clients.get(n);
                        nInfo.out.println("Ping");
                    }
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (noPong) {
                        for (String n : noPong) {
                            synchronized (clients) {
                                clients.remove(n);
                            }
                            System.out.println("removed " + n);
                        }
                    }
                    names = clients.keySet();
                    String list = "";
                    for (String n : names) {
                        list += n + ", ";
                    }
                    if (list.length() > 0) list = list.substring(0, list.length() - 2);
                    clientInfo.out.println("Current clients: " + list);
                    //clientInfo.out.println(names.size());
                } else if (line.equalsIgnoreCase("Pong")) {
                    synchronized (noPong) {
                        noPong.remove(clientName);
                    }
                    System.out.println("Pong from " + clientName);
                } else {
                    clientInfo.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
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
}
