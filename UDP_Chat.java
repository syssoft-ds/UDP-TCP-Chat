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
    private static final Map<String, ClientInfo> clients = new HashMap<>();
    private static final Map<String, String> predefinedAnswers = new HashMap<>();

    private record ClientInfo(String ip, int port) { }

    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static boolean isIP(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) { return false; }
        for (String p : parts) {
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
        if (args.length != 2) {
            fatal("Arguments: \"<port number> <client name>\"");
        }
        if (!isPort(args[0])) {
            fatal("Invalid port number");
        } else {
            port = Integer.parseInt(args[0]);
        }
        name = args[1];

        predefinedAnswers.put("Was ist deine MAC-Adresse?", "Meine MAC-Adresse ist: 00:00:00:00:00:00");
        predefinedAnswers.put("Sind Kartoffeln eine richtige Mahlzeit?", "Ja, Kartoffeln können eine vollständige Mahlzeit sein, je nach Zubereitung.");

        System.out.println(name + " (Port: " + port + ") is here, looking around.\nUse \"register <ip address> <port number>\" to contact another client.\nUse \"send <registered client name> <message>\" to message them.\nUse \"getclientlist\" to print current client list.\nUse \"send all <message>\" to send message to all registered clients.\nUse \"ask <Clientname> <Frage>\" to ask a predefined question.\nUse \"set <Frage> <Antwort>\" to set a predefined answer.\nUse \"quit\" to exit program.");

        new Thread(() -> receiveLines(port)).start();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) { // closes automatically
            String input;
            while (!(input = br.readLine()).equalsIgnoreCase("quit")) {
                String[] parts = input.split(" ");
                if (parts[0].equalsIgnoreCase("register") && parts.length == 3 && isPort(parts[2])) {
                    register(parts[1], Integer.parseInt(parts[2]));
                } else if (parts[0].equalsIgnoreCase("send") && !parts[1].equalsIgnoreCase("all")) {
                    String receiver = parts[1];
                    ClientInfo receiverInfo = clients.get(receiver);
                    if (receiverInfo != null) {
                        String message = input.substring(input.indexOf(receiver) + receiver.length()).trim();
                        sendLines(receiverInfo.ip, receiverInfo.port, message);
                        System.out.println("Sent \"" + message + "\" to " + receiver + ".");
                    } else {
                        System.err.println("Unknown client \"" + receiver + "\".");
                    }
                } else if (parts[0].equalsIgnoreCase("getclientlist")) {
                    updateClientList();
                    printClientList();
                } else if (parts[0].equalsIgnoreCase("send") && parts[1].equalsIgnoreCase("all")) {
                    if (parts.length >= 3) {
                        String message = input.substring(input.indexOf("send all") + 8).trim();
                        sendAll(message);
                    } else {
                        System.err.println("Usage: send all <message>");
                    }
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length >= 3) {
                    String clientName = parts[1];
                    String question = input.substring(input.indexOf(parts[2])).trim();
                    askPredefinedQuestion(clientName, question);
                } else if (parts[0].equalsIgnoreCase("set") && parts.length >= 3) {
                    String inputLine = input.substring(input.indexOf("set") + 3); // Remove "set "
                    setPredefinedAnswer(inputLine);
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
        try (DatagramSocket s = new DatagramSocket(port)) {
            byte[] buffer = new byte[packetSize];
            String line;
            do {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                s.receive(p);
                line = new String(buffer, 0, p.getLength(), StandardCharsets.UTF_8);
                if (line.startsWith("Hello, this is ")) {
                    String[] parts = line.split(", ");
                    String name = parts[1].split(" ")[2];
                    String ip = parts[2].split(" ")[4];
                    String clientPortString = parts[3].split(" ")[4];
                    if (isIP(ip) && isPort(clientPortString)) {
                        int clientPort = Integer.parseInt(clientPortString);
                        clients.put(name, new ClientInfo(ip, clientPort));
                    } else {
                        System.err.println("Cannot register \"" + name + "\" because of invalid information.");
                    }
                } else if (line.startsWith("Ping")) {
                    sendPongToAllClients(p.getAddress().getHostAddress(), line);
                }
                System.out.println(line);
            } while (!line.equalsIgnoreCase("quit"));
        } catch (IOException e) {
            System.err.println("Unable to receive message on port \"" + port + "\".");
        }
    }

    private static void sendLines(String friend, int friends_port, String message) {
        try (DatagramSocket s = new DatagramSocket()) {
            InetAddress ip = InetAddress.getByName(friend);
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket p = new DatagramPacket(buffer, buffer.length, ip, friends_port);
            s.send(p);
            System.out.println("Message sent.");
        } catch (IOException e) {
            System.err.println("Unable to send message to \"" + friend + "\".");
        }
    }

    private static void sendAll(String message) {
        for (ClientInfo client : clients.values()) {
            sendLines(client.ip, client.port, message);
        }
        System.out.println("Sent \"" + message + "\" to all clients.");
    }

    private static void sendPongToAllClients(String senderIP, String pingMessage) {
        for (ClientInfo client : clients.values()) {
            sendLines(client.ip, client.port, "Pong from " + senderIP);
        }
    }

    private static void askPredefinedQuestion(String clientName, String question) {
        String predefinedQuestion = clientName + " " + question;
        if (predefinedAnswers.containsKey(predefinedQuestion)) {
            String answer = predefinedAnswers.get(predefinedQuestion);
            System.out.println("Answering predefined question: \"" + question + "\" -> " + answer);
            // Send the predefined answer to the client
            ClientInfo client = clients.get(clientName);
            if (client != null) {
                sendLines(client.ip, client.port, answer);
            } else {
                System.err.println("Unknown client \"" + clientName + "\".");
            }
        } else {
            System.err.println("No predefined answer found for question: \"" + question + "\".");
        }
    }


    private static void setPredefinedAnswer(String input) {
        int questionEndIndex = input.indexOf('?');
        if (questionEndIndex != -1 && questionEndIndex < input.length() - 1) {
            String question = input.substring(0, questionEndIndex + 1).trim(); // Include '?'
            String answer = input.substring(questionEndIndex + 1).trim(); // Skip '?' and space
            predefinedAnswers.put(question, answer);
            System.out.println("Set predefined answer for question: \"" + question + "\" -> " + answer);
        } else {
            System.err.println("Invalid set command. Usage: set <Frage>? <Antwort>");
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

    private static void updateClientList() {
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            sendLines(entry.getValue().ip, entry.getValue().port, "Ping");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printClientList() {
        System.out.println("Current Client List:");
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue().ip + ":" + entry.getValue().port);
        }
        System.out.println();
    }
}
