package aufgabe4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UDP_Chat {

    private static int port;
    private static String name;
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static Map<String, String> answers = new HashMap<>();
    private static Set<String> pings = new HashSet();

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

        System.out.println(name + " (Port: " + port + ") is here, looking around.\nUse \"register <ip address> <port number>\" to contact another client.\nUse \"send <registered client name> <message>\" to message them.\nUse \"quit\" to exit program.");
        // Start a new thread to listen for messages
        new Thread(() -> receiveLines(port)).start();

        // Main thread continues to process user input
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) { // closes automatically
            String input;
            while (!(input = br.readLine()).equalsIgnoreCase("quit")) {
                String[] parts = input.split(" ");
                if (parts[0].equalsIgnoreCase("register") && parts.length == 3 && isPort(parts[2])) {
                    register(parts[1], Integer.parseInt(parts[2]));
                } else if (parts[0].equalsIgnoreCase("send")) {
                    String receiver = parts[1];
                    if (!receiver.equalsIgnoreCase("all")) {
                        ClientInfo receiverInfo = clients.get(receiver);
                        if (receiverInfo != null) {
                            String message = input.substring(input.indexOf(receiver) + receiver.length()).trim();
                            sendLines(receiverInfo.ip, receiverInfo.port, message);
                            System.out.println("Sent \"" + message + "\" to " + receiver + ".");
                        } else {
                            System.err.println("Unknown client \"" + receiver + "\".");
                        }
                    } else {
                        Collection<ClientInfo> clientInfos = clients.values();
                        String message = input.substring(input.indexOf("all") + receiver.length()).trim();
                        for (ClientInfo c : clientInfos) {
                            sendLines(c.ip, c.port, message);
                        }
                        System.out.println("Sent \"" + message + "\" to all.");
                    }
                } else if (parts[0].equalsIgnoreCase("getclientlist")) {
                    Collection<ClientInfo> clientInfos = clients.values();
                    for (ClientInfo c : clientInfos) {
                        sendLines(c.ip, c.port, "Ping " + name);
                    }
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Set<Map.Entry<String, ClientInfo>> clientEntries = clients.entrySet();
                    for (Map.Entry<String, ClientInfo> c : clientEntries) {
                        if (!pings.contains(c.getKey())) {
                            clients.remove(c.getKey());
                            System.out.println("deleted " + c.getKey());
                        }
                    }
                    System.out.println("updated clientlist");
                    Set<String> names = clients.keySet();
                    String list = "";
                    for (String n : names) {
                        list += n + ", ";
                    }
                    if (list.length() > 0) list = list.substring(0, list.length() - 2);
                    System.out.println("Current clients: " + list);
                } else if (parts[0].equalsIgnoreCase("set")) {
                    parts = input.substring(4).split("\\? ");
                    if (parts.length < 2) {
                        System.err.println("Missing argument.");
                    } else {
                        answers.put(parts[0] + "?", parts[1]);
                    }
                } else {
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
                if (line.startsWith("Ping ")) {
                    ClientInfo client = clients.get(line.substring(5).trim());
                    sendLines(client.ip, client.port, "Pong " + name);
                } else if (line.startsWith("Pong ")) {
                    pings.add(line.substring(5).trim());
                } else {
                    if (line.startsWith("Hello, this is ")) { // Register phrases
                        String[] parts = line.split(", ");
                        // saving for the important data
                        String name = parts[1].split(" ")[2];
                        String ip = parts[2].split(" ")[4];
                        String clientPortString = parts[3].split(" ")[4];
                        if (isIP(ip) && isPort(clientPortString)) { // validating
                            int clientPort = Integer.parseInt(clientPortString);
                            clients.put(name, new ClientInfo(ip, clientPort));
                        } else {
                            System.err.println("Cannot register \"" + name + "\" because of invalid information.");
                        }
                    }
                    System.out.println(line);
                    if (line.endsWith("?")) {
                        String message = "My answer to \"" + line + "\" is \"" + answers.get(line) + "\"";
                        if (answers.get(line) != null) {
                            Collection<ClientInfo> clientInfos = clients.values();
                            for (ClientInfo c : clientInfos) {
                                sendLines(c.ip, c.port, message);
                            }
                            System.out.println("Sent \"" + message + "\" automatically to all.");
                        }
                    }
                }
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

    private static void register(String friend, int friends_port) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String message = String.format("Hello, this is %s, my IPv4 address is %s, my port number is %d, and I am thrilled to talk to you.", name, ip, UDP_Chat.port);
            sendLines(friend, friends_port, message);
        } catch (UnknownHostException e) {
            System.err.println("Unable to find client \"" + friend + "\".");
        }
    }
}