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
    private static final Map<String, String> predefinedQA = new HashMap<>();

    private static class ClientInfo {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        long lastActiveTime;

        ClientInfo(Socket socket) {
            try {
                this.socket = socket;
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.lastActiveTime = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on IP " + InetAddress.getLocalHost().getHostAddress() + " on Port " + port + ".\nUse \"quit\" to exit program.");

            new Thread(TCP_Chat_Server::removeInactiveClients).start();

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
                clientInfo.lastActiveTime = System.currentTimeMillis();
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
                    clientInfo.out.println("You are registered as " + clientName);
                } else if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    String recipient = parts[1];
                    String message = parts[2];
                    sendMessage(clientName, recipient, message);
                } else if (parts[0].equalsIgnoreCase("broadcast") && parts.length == 2) {
                    String message = parts[1];
                    broadcastMessage(clientName, message);
                } else if (parts[0].equalsIgnoreCase("list")) {
                    sendClientList(clientInfo);
                } else if (parts[0].equalsIgnoreCase("set") && parts.length == 3) {
                    setPredefinedQA(parts[1], parts[2]);
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                    String recipient = parts[1];
                    String question = parts[2];
                    askQuestion(clientName, recipient, question);
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
                ClientInfo senderInfo = clients.get(sender);
                if (senderInfo != null) {
                    senderInfo.out.println("Client " + recipient + " not found.");
                }
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
        synchronized (clients) {
            StringBuilder clientList = new StringBuilder("Clients: ");
            for (String clientName : clients.keySet()) {
                clientList.append(clientName).append(" ");
            }
            clientInfo.out.println(clientList.toString());
        }
    }

    private static void setPredefinedQA(String question, String answer) {
        synchronized (predefinedQA) {
            predefinedQA.put(question.toLowerCase(), answer);
        }
    }

    private static void askQuestion(String sender, String recipient, String question) {
        synchronized (clients) {
            ClientInfo recipientInfo = clients.get(recipient);
            if (recipientInfo != null) {
                String answer = predefinedQA.get(question.toLowerCase());
                if (answer != null) {
                    recipientInfo.out.println("Question from " + sender + ": " + question);
                    sendMessage(recipient, sender, answer);
                } else {
                    sendMessage(recipient, sender, "I don't know the answer to that question.");
                }
            } else {
                ClientInfo senderInfo = clients.get(sender);
                if (senderInfo != null) {
                    senderInfo.out.println("Client " + recipient + " not found.");
                }
            }
        }
    }

    private static void removeInactiveClients() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            synchronized (clients) {
                clients.entrySet().removeIf(entry -> (currentTime - entry.getValue().lastActiveTime) > 30000); // 30 seconds timeout
            }
            try {
                Thread.sleep(10000); // Check every 10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
