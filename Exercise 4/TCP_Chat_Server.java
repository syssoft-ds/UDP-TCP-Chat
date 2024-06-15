import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
            try {
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
                        if (recipient.equals("all")){
                            for (String everyRecipient : clients.keySet()) {
                                sendMessage(clientName, everyRecipient, message);
                            }
                            continue;
                        }
                        sendMessage(clientName, recipient, message);
                    } else if (parts[0].equalsIgnoreCase("getclientlist") && parts.length == 1){
                        StringBuilder clientList = new StringBuilder("Clients: ");
                        for (String client : clients.keySet()) {
                            clientList.append(client).append(", ");
                        }
                        clientInfo.out.println(clientList);
                    } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                        String recipient = parts[1];
                        String question = parts[2];
                        sendMessage(clientName, recipient, question);
                    } else {
                        clientInfo.out.println("Unknown command.");
                    }
                }
            } catch (SocketException e) {
                clients.remove(clientName);
                System.out.println(clientName + " disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.currentThread().interrupt();
        //System.exit(0);
    }

    private static void sendMessage(String sender, String recipient, String message) {
        synchronized (clients) {
            ClientInfo recipientInfo = clients.get(recipient);
            if (recipientInfo != null) {
                recipientInfo.out.println("Message from " + sender + ": " + message);
            } else {
                clients.get(sender).out.println("Client " + recipient + " not found.");
                System.out.println("Client " + recipient + " not found.");
            }
        }
    }
}
