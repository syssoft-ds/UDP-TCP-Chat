import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static final Map<String, String> predefinedQuestions = new HashMap<>();

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
        predefinedQuestions.put("What is your MAC address?", "I don't have a MAC address.");
        predefinedQuestions.put("Are potatoes a proper meal?", "Absolutely, potatoes are versatile and nutritious!");

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
                } else if (parts[0].equalsIgnoreCase("sendall") && parts.length == 2) {
                    String message = parts[1];
                    sendToAll(clientName, message);
                } else if (parts[0].equalsIgnoreCase("updateclients")) {
                    updateClients();
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

    private static void sendToAll(String sender, String message) {
        synchronized (clients) {
            for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
                entry.getValue().out.println("Message from " + sender + ": " + message);
            }
        }
    }

    private static void updateClients() {
        synchronized (clients) {
            Iterator<Map.Entry<String, ClientInfo>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ClientInfo> entry = iterator.next();
                try {
                    entry.getValue().out.println("ping");
                } catch (Exception e) {
                    iterator.remove();
                    System.out.println("Removed \"" + entry.getKey() + "\" from client list due to no response.");
                }
            }
        }
    }

    private static void handlePredefinedQuestions(String message, ClientInfo clientInfo) {
        for (Map.Entry<String, String> entry : predefinedQuestions.entrySet()) {
            if (message.equalsIgnoreCase(entry.getKey())) {
                clientInfo.out.println(entry.getValue());
                System.out.println("Responded to predefined question \"" + entry.getKey() + "\" with \"" + entry.getValue() + "\".");
            }
        }
    }
}
