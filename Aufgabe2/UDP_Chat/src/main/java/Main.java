import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final int packetSize = 4096;
    private static Map<String, Contact> contacts = new HashMap<>();
    private static String myName;
    private static int myPort;
    private static DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: <name> <port>");
        myName = args[0];
        myPort = Integer.parseInt(args[1]);

        // Create the DatagramSocket on the specified port for both sending and receiving
        socket = new DatagramSocket(myPort);

        Thread listenerThread = new Thread(() -> {
            try {
                listenAndTalk();
            } catch (IOException e) {
                System.out.println("Error in listener: " + e.getMessage());
            }
        });
        listenerThread.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command;
        while (true) {
            System.out.print("Enter command: ");
            command = br.readLine();
            if (command.startsWith("register ")) {
                String[] parts = command.split(" ");
                if (parts.length == 4) {
                    String contactName = parts[1];
                    String contactIp = parts[2];
                    int contactPort = Integer.parseInt(parts[3]);
                    sendRegistration(contactName, contactIp, contactPort);
                } else {
                    System.out.println("Usage: register <name> <ip> <port>");
                }
            } else if (command.startsWith("send ")) {
                String[] parts = command.split(" ", 3);
                if (parts.length == 3) {
                    String contactName = parts[1];
                    String message = parts[2];
                    sendMessage(contactName, message);
                } else {
                    System.out.println("Usage: send <name> <message>");
                }
            } else if (command.equalsIgnoreCase("bye")) {
                System.out.println("Exiting...");
                System.exit(0);
            } else {
                System.out.println("Unknown command.");
            }
        }
    }

    private static void sendRegistration(String contactName, String contactIp, int contactPort) throws IOException {
        InetAddress address = InetAddress.getByName(contactIp);
        String registrationMessage = "REGISTER " + myName + " " + InetAddress.getLocalHost().getHostAddress() + " " + myPort;
        byte[] buffer = registrationMessage.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, contactPort);
        socket.send(packet);
        System.out.println("Sent registration to " + contactName);
    }

    private static void sendMessage(String contactName, String message) throws IOException {
        if (contacts.containsKey(contactName)) {
            Contact contact = contacts.get(contactName);
            InetAddress address = InetAddress.getByName(contact.getIp());
            String fullMessage = myName + ": " + message;
            byte[] buffer = fullMessage.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, contact.getPort());
            socket.send(packet);
            System.out.println("Sent message to " + contactName);
        } else {
            System.out.println("Contact not found: " + contactName);
        }
    }

    private static void listenAndTalk() throws IOException {
        byte[] buffer = new byte[packetSize];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String message = new String(buffer, 0, packet.getLength(), "UTF-8");
            processMessage(message, packet.getAddress().getHostAddress(), packet.getPort());
        }
    }

    private static void processMessage(String message, String senderIp, int senderPort) {
        if (message.startsWith("REGISTER ")) {
            String[] parts = message.split(" ");
            if (parts.length == 4) {
                String contactName = parts[1];
                String contactIp = parts[2];
                int contactPort = Integer.parseInt(parts[3]);
                register(contactName, contactIp, contactPort);
            } else {
                System.out.println("Invalid registration message format.");
            }
        } else {
            System.out.println("Received: " + message);
        }
    }

    private static void register(String contactName, String contactIp, int contactPort) {
        contacts.put(contactName, new Contact(contactIp, contactPort));
        System.out.println("Registered " + contactName + " with IP " + contactIp + " and port " + contactPort);
    }

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    static class Contact {
        private String ip;
        private int port;

        public Contact(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }
}
