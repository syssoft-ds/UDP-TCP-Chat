import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPChatServer {
    private static ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java TCPChatServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("Enter your name:");
                clientName = in.readLine().trim();
                clients.put(clientName, socket);
                out.println("Welcome " + clientName + "! You are now registered.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("send")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length < 3) {
                            out.println("Invalid send command. Usage: send <username> <message>");
                            continue;
                        }
                        String recipient = parts[1];
                        String msg = parts[2];

                        Socket recipientSocket = clients.get(recipient);
                        if (recipientSocket != null) {
                            PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                            recipientOut.println(clientName + ": " + msg);
                        } else {
                            out.println("Recipient not found: " + recipient);
                        }
                    } else {
                        out.println("Invalid command. Use 'send <username> <message>' to send a message.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (clientName != null) {
                    clients.remove(clientName);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
