import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UDP_Chat {

    private static int port;
    private static String name;
    private static Map<String, ClientInfo> clients = new HashMap<>();
    private static Map<String, Boolean> clientStatus = new HashMap<>();

    private record ClientInfo(String ip, int port) { }


    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static boolean isIP(String ip) { // Checks if String is valid IPv4 address
        String[] parts = ip.split("\\."); // Split by dot
        if (parts.length != 4) { return false; } // Must be 4 chunks
        for (String p : parts) { // Check if numbers are valid
            try {
                int number = Integer.parseInt(p);
                if (number < 0 || number > 255) { return false; }
            } catch (NumberFormatException e) { return false; }
        }
        return true;
    }

    public static boolean isPort(String port) {
        try {
            int number = Integer.parseInt(port);
            if (number < 0 || number > 65535) { return false; }
        } catch (NumberFormatException e) { return false; }
        return true;
    }

    public static void main(String[] args) {

        // Handling arguments, checking validity
        if (args.length != 2) {
            fatal("Arguments: \"<port number> <client name>\"");
        }
        if (!isPort(args[0])) {
            fatal("Invalid port number");
        } else {
            port = Integer.parseInt(args[0]);
        }
        name = args[1];

        System.out.println(name + " (Port: " + port + ") is here, looking around.\nUse \"register <ip address> <port number>\" to contact another client.\nUse \"send <registered client name> <message>\" to message them.\nUse \"sendAll\" <message> to message all clients \nUse \"quit\" to exit program.");
        // Start a new thread to listen for messages
        new Thread(() -> receiveLines(port)).start();

        // Main thread continues to process user input
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) { // closes automatically
            String input;
            while (!(input = br.readLine()).equalsIgnoreCase("quit")) {
                String[] parts = input.split(" ");
                if (parts[0].equalsIgnoreCase("register") && parts.length == 3 && isPort(parts[2])) {
                    register(parts[1], Integer.parseInt(parts[2]), false);
                } else if (parts[0].equalsIgnoreCase("send")) {
                    String receiver = parts[1];
                    ClientInfo receiverInfo = clients.get(receiver);
                    if (receiverInfo != null) {
                        String message = input.substring(input.indexOf(receiver) + receiver.length()).trim();
                        sendLines(receiverInfo.ip, receiverInfo.port, message);
                        System.out.println("Sent \"" + message + "\" to " + receiver + ".");
                    }
                    else {
                        System.err.println("Unknown client \"" + receiver + "\".");
                    }
                }
                else if (parts[0].equalsIgnoreCase("sendAll")) {
                    String message = input.substring(input.indexOf("sendAll") + "sendAll".length()).trim();
                    sendAll(message);
                    System.out.println("Sent \"" + message + "\" to all clients.");
                }
                else if (parts[0].equalsIgnoreCase("update")){
                    updateAll();
                }
                else {
                    System.err.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static final int packetSize = 4096;

    private static void receiveLines(int port) {
        try(DatagramSocket s = new DatagramSocket(port)) { // closes automatically
            byte[] buffer = new byte[packetSize];
            String line;
            do {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                s.receive(p);
                line = new String(buffer, 0, p.getLength(), StandardCharsets.UTF_8);
                if (line.startsWith("Hello, this is ")) { // Register phrases
                    String[] parts = line.split(", ");
                    // saving for the important data
                    String name = parts[1].split(" ")[2];
                    String ip = parts[2].split(" ")[4];
                    String clientPortString = parts[3].split(" ")[4];
                    if (isIP(ip) && isPort(clientPortString)) { // validating
                        int clientPort = Integer.parseInt(clientPortString);

                        if (parts[4].equals("and I am thrilled to talk to you now.")) { // if the client is new
                            register(ip, clientPort, true);
                        }
                        clients.put(name, new ClientInfo(ip, clientPort));
                        clientStatus.put(name, true);
                    } else {
                        System.err.println("Cannot register \"" + name + "\" because of invalid information.");
                    }
                }
                else if (line.startsWith("update")) {
                    String[] parts = line.split(" ");
                    String clientName = parts[1];
                    respondToClient(clientName, true);
                }
                else if (line.startsWith("response")) {
                    String[] parts = line.split(" ");
                    String clientName = parts[1];
                    clientStatus.put(clientName, true);
                }
                System.out.println(line);
            } while (!line.equalsIgnoreCase("quit"));
        } catch (IOException e) {
            System.err.println("Unable to receive message on port \"" + port + "\".");
        }
    }

    private static void sendLines(String friend, int friends_port, String message) {
        try (DatagramSocket s = new DatagramSocket()) { // closes automatically
            InetAddress ip = InetAddress.getByName(friend);
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket p = new DatagramPacket(buffer, buffer.length, ip, friends_port);
            s.send(p);
            System.out.println("Message sent.");
        } catch (IOException e) {
            System.err.println("Unable to send message to \"" + friend + "\".");
        }
    }

    private static void register(String friend, int friends_port, boolean isResponse) {
        String fistMessage = "";
        if (!isResponse){
            fistMessage = "now";
        }
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String message = String.format("Hello, this is %s, my IPv4 address is %s, my port number is %d, and I am thrilled to talk to you %s.", name, ip, UDP_Chat.port, fistMessage);
            sendLines(friend, friends_port, message);
        } catch (UnknownHostException e) {
            System.err.println("Unable to find client \"" + friend + "\".");
        }
    }

    private static void sendAll(String message) {
        for (ClientInfo clientInfo : clients.values()) {
            sendLines(clientInfo.ip, clientInfo.port, message);
        }
    }


    private static void updateAll() {
        for (Map.Entry<String, Boolean> entry : clientStatus.entrySet()) {
            entry.setValue(false);
            System.out.println("Setting " + entry.getKey() + " to false.");
        }
        for (String clientName : clients.keySet()) {
            respondToClient(clientName, false);
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

    private static void respondToClient(String clientName, boolean isResponse) {
        if (isResponse){
            sendLines(clients.get(clientName).ip, clients.get(clientName).port, "response " + name);
        }
        else {
            sendLines(clients.get(clientName).ip, clients.get(clientName).port, "update " + name);
        }
    }
}