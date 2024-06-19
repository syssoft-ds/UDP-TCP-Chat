import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UDPChat {
    private static Map<String, InetSocketAddress> users = new HashMap<>();
    private static String username;

    public static void receiveLines(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[4096];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                handleReceivedMessage(message, packet.getAddress(), packet.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendLines(String host, int port) {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {
            InetAddress address = InetAddress.getByName(host);
            String registerMessage = "register " + username;
            sendMessage(socket, address, port, registerMessage);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                handleSendMessage(line, socket, address, port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleReceivedMessage(String message, InetAddress address, int port) {
        if (message.startsWith("register")) {
            String[] parts = message.split(" ", 2);
            String user = parts[1];
            users.put(user, new InetSocketAddress(address, port));
            System.out.println("User " + user + " registered with IP " + address + " and port " + port);
        } else if (message.startsWith("send")) {
            String[] parts = message.split(" ", 3);
            String recipient = parts[1];
            String msg = parts[2];
            InetSocketAddress recipientAddress = users.get(recipient);
            if (recipientAddress != null) {
                sendMessage(recipientAddress, msg);
            } else {
                System.out.println("Recipient not found: " + recipient);
            }
        } else {
            System.out.println("Message <" + message + "> received from " + address + ":" + port);
        }
    }

    private static void handleSendMessage(String line, DatagramSocket socket, InetAddress address, int port) {
        if (line.startsWith("send")) {
            String[] parts = line.split(" ", 3);
            String recipient = parts[1];
            String message = parts[2];
            InetSocketAddress recipientAddress = users.get(recipient);
            if (recipientAddress != null) {
                sendMessage(socket, recipientAddress.getAddress(), recipientAddress.getPort(), "send " + username + " " + message);
            } else {
                System.out.println("Recipient not found: " + recipient);
            }
        } else {
            sendMessage(socket, address, port, "register " + username);
        }
    }

    private static void sendMessage(DatagramSocket socket, InetAddress address, int port, String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println("Message <" + message + "> sent to " + address + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(InetSocketAddress recipientAddress, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            sendMessage(socket, recipientAddress.getAddress(), recipientAddress.getPort(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java UDPChat <username> -l <port> or java UDPChat <username> <ip> <port>");
            return;
        }

        username = args[0];

        if (args[1].equals("-l")) {
            int port = Integer.parseInt(args[2]);
            System.out.println(username + " started as a server on port " + port);
            receiveLines(port);
        } else {
            String host = args[1];
            int port = Integer.parseInt(args[2]);
            System.out.println(username + " started as a client connecting to " + host + ":" + port);
            sendLines(host, port);
        }
    }
}
