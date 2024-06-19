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
    private static final Map<String, String> predefinedAnswers = new HashMap<>();//42

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
            predefinedAnswers.put("what is the answer to all questions?", "42");
            System.out.println("ask me (server): what is the answer to all questions?");
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
                System.out.println("Received: " + line);
                String[] parts = line.split(" ", 3);//command + target + payload
                if (parts[0].equalsIgnoreCase("getclientlist") && parts.length == 1) {
                    getClientList(clientName);
                } else if (parts[0].equalsIgnoreCase("register") && parts.length == 2) {
                    clientName = parts[1];
                    if (clients.containsKey(clientName)) {
                        clientName = clientName + (int) (Math.random() * 1000); // add random number if name is taken
                    }
                    synchronized (clients) {
                        clients.put(clientName, clientInfo);
                    }
                    System.out.println(clientName + " registered.");

                } else if (parts.length == 3) {
                    String recipient = parts[1];
                    if (parts[0].equalsIgnoreCase("ask")) {
                        String question = parts[2];
                        if (recipient.equalsIgnoreCase("server")) {
                            String answer = predefinedAnswers.get(question);
                            if (answer != null) {
                                sendMessage("Server", clientName, "42");
                            }
                        } else {
                            sendMessage(clientName, recipient, "question " + question);
                        }
                    } else if (parts[0].equalsIgnoreCase("send")) {
                        String message = parts[2];
                        if (parts[1].equalsIgnoreCase("all")) {
                            broadcast(clientName, message);
                        } else {
                            sendMessage(clientName, recipient, message);
                        }
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

    private static void broadcast(String sender, String message) {
        synchronized (clients) {
            //recipients are all clients except the sender
            for (String recipient : clients.keySet()) {
                if (!recipient.equals(sender)) {
                    sendMessage(sender, recipient, message);
                }
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

    private static void getClientList(String requester) {
        synchronized (clients) {
            Iterator<Map.Entry<String, ClientInfo>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ClientInfo> entry = iterator.next();
                String clientName = entry.getKey();
                ClientInfo clientInfo = entry.getValue();
                try {
                    clientInfo.out.println("ping");
                    Thread.sleep(100);//in case the response isn't that fast
                    String response = clientInfo.in.readLine();
                    if (!"pong".equals(response)) {
                        iterator.remove();
                        System.out.println("Client " + clientName + " removed due to no response.");
                    }
                } catch (IOException | InterruptedException e) {
                    iterator.remove();
                    System.out.println("Client " + clientName + " removed due to an error.");
                }
            }
            ClientInfo requesterInfo = clients.get(requester);
            if (requesterInfo != null) {
                requesterInfo.out.println("clientlist " + String.join(", ", clients.keySet()));
            }
        }
    }
}
