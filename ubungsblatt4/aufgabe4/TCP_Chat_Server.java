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
    private static Map<String, Boolean> clientStatus = new HashMap<>();
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
                } else if (parts[0].equalsIgnoreCase("sendAll") && parts.length == 2) {
                    String message = parts[1];
                    sendAll(message);
                }
                else if (parts[0].equalsIgnoreCase("update")){
                    updateAll();
                }
                else {
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

    private static void sendAll(String message) {
        synchronized (clients) {
            for (ClientInfo clientInfo : clients.values()) {
                clientInfo.out.println(message);
            }
        }
    }

    private static void updateAll() {
        synchronized (clients) {
            for (String client : clients.keySet()) {
                clientStatus.put(client,stillConnected(client));
            }
        }
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Updating all clients.");
                removeInactive();
            } catch (InterruptedException e) {
                System.out.println("Error in updateAll");
            }
        }).start();

    }

    private static void removeInactive() {
        for (Map.Entry<String, Boolean> entry : clientStatus.entrySet()) {
            if (!entry.getValue()) {
                if (clients.containsKey(entry.getKey())) {
                    System.out.println("Removing " + entry.getKey() + " from clients.");
                    clients.remove(entry.getKey());
                }
            }
        }
    }
    private static boolean stillConnected(String client) {
        try {
            clients.get(client).out.println("1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
