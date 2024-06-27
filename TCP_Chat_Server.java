
//funktionirt nicht vollständig
import java.io.*;
import java.net.*;
import java.util.*;

public class TCP_Chat_Server {
    private static final int port = 1444;
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static final Map<String, String> predefinedAnswers = new HashMap<>();

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
    private static void updateClientList(Socket sender) {
        List<String> clientNames = new ArrayList<>();

        Iterator<Map.Entry<String, ClientInfo>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ClientInfo> entry = iterator.next();
            Socket client = entry.getValue().socket;
            if (!client.equals(sender)) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    clientNames.add(in.readLine());
                } catch (IOException e) {
                    System.err.println("Unable to read client name: " + e.getMessage());
                }
            }
        }

        try (PrintWriter out = new PrintWriter(sender.getOutputStream(), true)) {
            out.println("clientlist: " + String.join(" ", clientNames));
        } catch (IOException e) {
            System.err.println("Unable to send client list: " + e.getMessage());
        }
    }
    private static void broadcastMessage(String message, Socket sender) {
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            Socket client = entry.getValue().socket;
            if (!client.equals(sender)) {
                try (PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
                    out.println(message);
                } catch (IOException e) {
                    System.err.println("Unable to send message to client: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int serverPort = 1444;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Chat Server is listening on port " + serverPort);

            while (true) {
                Socket socket = serverSocket.accept();
                clients.put(socket.getInetAddress().toString(), new ClientInfo(socket));
                predefinedAnswers.put("What is your MAC address?", "Your MAC address is: 00:0a:95:9d:68:16");
                predefinedAnswers.put("Are carrots a good meal?", "Carrots are not a good meal.");

                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Unable to start the server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            handleClient(socket);
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
                        clientName = clientName + (int) (Math.random() * 1000);

                    }
                    synchronized (clients) {
                        clients.put(clientName, clientInfo);
                    }
                    System.out.println(clientName + " registered.");
                } else if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    String recipient = parts[1];
                    String message = parts[2];
                    sendMessage(clientName, recipient, message);
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 2) {
                    String question = parts[1];
                    String answer = predefinedAnswers.get(question);
                    if (answer != null) {
                        clientInfo.out.println("Predefined answer: " + answer);
                    } else {
                        clientInfo.out.println("Unknown question.");
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
