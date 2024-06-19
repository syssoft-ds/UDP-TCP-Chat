import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
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
                } else if (parts[0].equalsIgnoreCase("sendall") && parts.length == 2) {
                    updateClients();
                    sendAll(clientInfo, clientName, parts[1]);
                } else if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    String recipient = parts[1];
                    String message = parts[2];
                    sendMessage(clientName, recipient, message);
                } else if (parts[0].equalsIgnoreCase("pong") && parts.length == 1) {
                    System.out.println("pong received from " + clientName);
                } else {
                    clientInfo.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

private static void updateClients() {
    synchronized (clients) {
        System.out.println("Clients before update: " + clients.keySet());
        Iterator<Map.Entry<String, ClientInfo>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ClientInfo> entry = iterator.next();
            ClientInfo clientInfo = entry.getValue();
            try {
                clientInfo.out.println("ping");
                clientInfo.socket.setSoTimeout(3000); // Set timeout to 3 seconds
                String response = clientInfo.in.readLine();
                if (response == null || !response.equals("pong")) {
                    iterator.remove();
                }
            } catch (SocketTimeoutException e) {
                // Client did not respond in time, remove it from the list
                iterator.remove();
            } catch (IOException e) {
                // Other IOException, print stack trace
                e.printStackTrace();
            }
        }
        System.out.println("Clients after update: " + clients.keySet());
    }
}

    private static void sendAll(ClientInfo clientInfo, String clientName, String message) {
        synchronized (clients) {
            for (ClientInfo recipientInfo : clients.values()) {
                if (recipientInfo != clientInfo) {
                    recipientInfo.out.println("Message from " + clientName + " : " + message);
                }
            }
        }
    }

    private static void sendMessage(String sender, String recipient, String message) {
        synchronized (clients) {
            ClientInfo recipientInfo = clients.get(recipient);
            if (recipientInfo != null) {
                recipientInfo.out.println("Message from " + sender + " : " + message);
            } else {
                System.out.println("Client " + recipient + " not found.");
            }
        }
    }
}