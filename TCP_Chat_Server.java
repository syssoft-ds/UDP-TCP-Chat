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
import java.util.concurrent.ConcurrentHashMap;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

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

        new Thread(() -> {
            while (true) {
                checkClientStatus();
                try {
                    Thread.sleep(10000); // Check every 10 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
                    if(respondToPredefinedQuestion(clientName, recipient, message)){
                        sendMessage(clientName, recipient, message);
                    }
                    sendMessage(recipient, clientName, message);
                } else if (parts[0].equalsIgnoreCase("broadcast") && parts.length == 2) {
                    String message = parts[1];
                    broadcastMessage(clientName, message);
                } else if (parts[0].equalsIgnoreCase("getclients")) {
                    sendClientList(clientInfo);

                } else {
                    clientInfo.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean respondToPredefinedQuestion(String sender, String recipient, String message) {
        Map<String, String> predefinedQuestions = new HashMap<>();
        predefinedQuestions.put("Was ist deine MAC-Adresse?", "Netter Versuch.");
        predefinedQuestions.put("Sind Kartoffeln eine richtige Mahlzeit?", "Ja, Kartoffeln können eine sättigende und nahrhafte Mahlzeit sein.");
        predefinedQuestions.put("Rei oder Asuka?", "Rei");

        if (predefinedQuestions.containsKey(message)) {
            ClientInfo recipientInfo = clients.get(sender);
            if (recipientInfo != null) {
                recipientInfo.out.println("Message from " + recipient + ": " + predefinedQuestions.get(message));
            } else {
                System.out.println("Client " + recipient + " not found.");
            }
            return true;
        }
        return false;
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

    private static void broadcastMessage(String sender, String message) {
        synchronized (clients) {
            for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
                entry.getValue().out.println("Broadcast from " + sender + ": " + message);
            }
        }
    }

    private static void sendClientList(ClientInfo clientInfo) {
        StringBuilder clientList = new StringBuilder("ClientList:");
        synchronized (clients) {
            for (String clientName : clients.keySet()) {
                clientList.append(clientName).append(",");
            }
        }
        clientInfo.out.println(clientList);
    }

    // Method to periodically check client status
    private static void checkClientStatus() {
        synchronized (clients) {
            Iterator<Map.Entry<String, ClientInfo>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ClientInfo> entry = iterator.next();
                ClientInfo clientInfo = entry.getValue();
                try {
                    clientInfo.out.println("ping");
                    if (!clientInfo.in.ready()) {
                        iterator.remove();
                        System.out.println("Client " + entry.getKey() + " removed due to no response.");
                    }
                } catch (IOException e) {
                    iterator.remove();
                    System.out.println("Client " + entry.getKey() + " removed due to no response.");
                }
            }
        }
    }
}
