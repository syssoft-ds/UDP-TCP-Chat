package U4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static Map<String, Boolean> statusClients = new HashMap<>();

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
                    if (respondToPredefinedQuestion(clientName, recipient, message)){
                        sendMessage(clientName, recipient, message);
                    }
                    sendMessage(clientName, recipient, message);
                } else if (parts[0].equalsIgnoreCase("sendAll") && parts.length == 2){
                    String message = parts[1];
                    sendAll(message);
                } else if (parts[0].equalsIgnoreCase("update")){
                    update();
                } else {
                    clientInfo.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static boolean respondToPredefinedQuestion(String clientName, String recipient, String message) {
        Map<String, String> Questions = new HashMap<>();
        Questions.put("Was ist deine MAC-Adresse?", "Netter Versuch.");
        Questions.put("Sind Kartoffeln eine richtige Mahlzeit?", "Ja natürlich.");

        if (Questions.containsKey(message)){
            ClientInfo recipientInfo = clients.get(clientName);
            if (recipientInfo != null) {
                recipientInfo.out.println("Message from " + recipient + ": " + Questions.get(message));
            }  else {
                System.out.println("Client " + recipient + " not found.");
            }
            return true;
        }
        return false;
    }

    private static void update() {
        synchronized (clients){
            statusClients.clear();
            for (String client : clients.keySet()) {
                statusClients.put(client,isConnected(client));
            }
            statusClients.entrySet().removeIf(entry -> !entry.getValue() && clients.containsKey(entry.getKey()));
        }
    }

    private static boolean isConnected(String client){
        try {
            Socket socket = clients.get(client).socket;
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                socket.sendUrgentData(0);
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private static void sendAll(String message) {
        synchronized (clients) {
            for (ClientInfo clientInfo : clients.values()) {
                clientInfo.out.println(message);
            }
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

}