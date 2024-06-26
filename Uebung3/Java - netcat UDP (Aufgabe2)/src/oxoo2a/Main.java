package oxoo2a;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.net.Socket;


public class Main {

    private static final int PACKET_SIZE = 4096;
    private static final Map<String, InetAddress> userAddresses = new HashMap<>();
    private static BufferedReader br = null;
    private static String userName;
    private static InetAddress serverAddress;
    private static int serverPort;
    private static PrintWriter writer;
    private static BufferedReader reader;


    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if ((args.length != 2) == (args.length != 3))
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port> <Username>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else{
            userName = args[2];
            connectAndTalk(args[0], port, userName);
        }
    }

    private static final int packetSize = 4096;

    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void listenAndTalk ( int port )  {
        ChatServer server = new ChatServer(port);
        server.execute();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk ( String other_host, int other_port , String user) throws IOException {


        serverAddress = InetAddress.getByName(other_host);
        serverPort = other_port;
        userName = user;
        Socket socket = new Socket(serverAddress, serverPort);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer.println(userName);
        System.out.println("Welcome to the chat app, " + userName + "!");
        new Thread(new ClientReceiver()).start();


        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            line = readString();
            if (line.startsWith("send ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length != 3) {
                    System.out.println("Invalid command format. Usage: send [name] [message]");
                    continue;
                }

                String recipientName = parts[1];
                String message = parts[2];

                InetAddress recipientAddress = userAddresses.get(recipientName);
                if (recipientAddress == null) {
                    System.out.println("User " + recipientName + " not found.");
                    continue;
                }

                String formattedMessage = userName + ": " + message;
                byte[] messageBytes = formattedMessage.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, recipientAddress, serverPort);
                s.send(packet);
            } else {
                System.out.println("Invalid command. Usage: send [name] [message]");
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static String readString () {
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }
    public static class ClientReceiver implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
class ChatServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java ChatServer <port-number>");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);
        ChatServer server = new ChatServer(port);
        server.execute();
    }

    void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    void addUserName(String userName) {
        userNames.add(userName);
    }

    void removeUser(String userName, UserThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " quitted");
        }
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }

    UserThread getUserThread(String userName) {
        for (UserThread aUser : userThreads) {
            if (aUser.userName.equals(userName)) {
                return aUser;
            }
        }
        return null;
    }
}

class UserThread extends Thread {
private Socket socket;
private ChatServer server;
private PrintWriter writer;
String userName;

public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        }

public void run() {
        try {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);

        printUsers();

        userName = reader.readLine();
        server.addUserName(userName);

        String serverMessage = "New user connected: " + userName;
        server.broadcast(serverMessage, this);

        String clientMessage;

        do {
        clientMessage = reader.readLine();
        if (clientMessage.startsWith("send ")) {
        String[] parts = clientMessage.split(" ", 3);
        if (parts.length == 3) {
        String recipientName = parts[1];
        String message = parts[2];
        sendMessageToUser(recipientName, userName + ": " + message);
        } else {
        writer.println("Invalid command format. Usage: send [name] [message]");
        }
        } else {
        serverMessage = "[" + userName + "]: " + clientMessage;
        server.broadcast(serverMessage, this);
        }

        } while (!clientMessage.equals("bye"));

        server.removeUser(userName, this);
        socket.close();

        serverMessage = userName + " has quitted.";
        server.broadcast(serverMessage, this);

        } catch (IOException ex) {
        System.out.println("Error in UserThread: " + ex.getMessage());
        ex.printStackTrace();
        }
        }

        void sendMessage(String message) {
        writer.println(message);
        }

private void printUsers() {
        if (server.hasUsers()) {
        writer.println("Connected users: " + server.getUserNames());
        } else {
        writer.println("No other users connected");
        }
        }

private void sendMessageToUser(String recipientName, String message) {
        UserThread recipientThread = server.getUserThread(recipientName);
        if (recipientThread != null) {
        recipientThread.sendMessage(message);
        } else {
        writer.println("User " + recipientName + " not found.");
        }
        }
        }
