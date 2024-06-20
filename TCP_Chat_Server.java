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

    private static class ClientInfo {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        Map<String, String> answers;
        boolean isAlive;


        ClientInfo(Socket socket) {
            try {
                this.socket = socket;
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.answers = new HashMap<>();
                this.isAlive = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on IP " + InetAddress.getLocalHost().getHostAddress() + " on Port "
                    + port + ".\nUse \"quit\" to exit program.");

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
                } else if (parts[0].equalsIgnoreCase("getclientlist")) {
                    // Send all clients a Ping message
                    for (ClientInfo client : clients.values()) {
                        client.isAlive = false;
                        client.out.println("Ping");
                    }
                    // Create a new Thread which waits for all clients to respond by sleeping for 1
                    // second
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (String cname : clients.keySet()) {
                            ClientInfo client = clients.get(cname);
                            if (!client.isAlive && !client.equals(clientInfo)) {
                                // Remove client from list
                                synchronized (clients) {
                                    clients.remove(cname);
                                }
                            }
                        }

                        // Send client name list to client
                        StringBuilder clientList = new StringBuilder();
                        clientList.append("Clients: ");
                        for (String cname : clients.keySet()) {
                            clientList.append(cname).append(", ");
                        }
                        clientList.deleteCharAt(clientList.length() - 1);
                        clientList.deleteCharAt(clientList.length() - 1);
                        clientInfo.out.println(clientList.toString());

                    }).start();

                } else if (parts[0].equalsIgnoreCase("pong")) {
                    clientInfo.isAlive = true;
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                    ClientInfo client = clients.get(parts[1]);
                    if (client.answers.containsKey(parts[2])) {
                        clientInfo.out.println("Answer from " + parts[1] + ": " + client.answers.get(parts[2]));
                    } else {
                        sendMessage(clientName, parts[1], parts[2]);
                    }
                } else if (parts[0].equalsIgnoreCase("set") && parts.length == 3) {
                    synchronized (clientInfo.answers) {
                        clientInfo.answers.put(parts[1], parts[2]);
                    }
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
