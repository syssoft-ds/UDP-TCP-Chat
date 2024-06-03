import java.io.*;
        import java.net.*;
        import java.util.*;
        import java.util.concurrent.*;

public class Main {

    private static final int SERVER_PORT = 12345;
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            startServer();
        } else if (args.length == 2) {
            String name = args[1];
            startClient(args[0], name);
        } else {
            System.out.println("Usage: java ChatProgram [server | client <name>]");
        }
    }

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startClient(String serverAddress, String name) {
        try (Socket socket = new Socket(serverAddress, SERVER_PORT)) {
            System.out.println("Connected to server.");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("register " + name);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = consoleReader.readLine()) != null) {
                String[] parts = input.split(" ", 3);
                if (parts.length == 3 && parts[0].equals("send")) {
                    writer.println("send " + parts[1] + " " + parts[2]);
                } else {
                    System.out.println("Invalid command. Usage: send <recipient> <message>");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private String name;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }

        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" ", 3);
                    if (parts[0].equals("register")) {
                        name = parts[1];
                        clients.put(name, this);
                        System.out.println(name + " registered.");
                    } else if (parts[0].equals("send")) {
                        String recipient = parts[1];
                        String message = parts[2];
                        sendMessage(recipient, name + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (name != null) {
                    clients.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessage(String recipient, String message) {
            ClientHandler clientHandler = clients.get(recipient);
            if (clientHandler != null) {
                clientHandler.writer.println(message);
            } else {
                writer.println("User " + recipient + " not found.");
            }
        }
    }
}
