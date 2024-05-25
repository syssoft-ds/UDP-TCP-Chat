package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static String name;
    private static Map<String, InetAddress> addressBook = new HashMap<>();
    private static Map<String, Integer> portBook = new HashMap<>();
    private static final int packetSize = 4096;

    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            fatal("Usage: \"<netcat> -l <port> <name>\" or \"netcat <ip> <port> <name>\"");
        int port = Integer.parseInt(args[1]);
        name = args[2];
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else
            connectAndTalk(args[0], port);
    }

    private static void listenAndTalk(int port) throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        System.out.println(name + " is listening on port " + port);

        Thread receiver = new Thread(() -> {
            try {
                byte[] localBuffer = new byte[packetSize];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(localBuffer, localBuffer.length);
                    socket.receive(packet);
                    String line = new String(localBuffer, 0, packet.getLength(), "UTF-8");
                    System.out.println(name + " received packet: " + line + " from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                    processMessage(line, packet.getAddress(), packet.getPort(), socket);
                }
            } catch (IOException e) {
                System.err.println(name + " encountered an error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        receiver.start();

        while (true) {
            String line = readString();
            if (line.equalsIgnoreCase("stop")) break;
            processCommand(line, socket, port);
        }
        socket.close();
    }


    private static void connectAndTalk(String otherHost, int otherPort) throws IOException {
        InetAddress otherAddress = InetAddress.getByName(otherHost);
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = new byte[packetSize];

        // Register with the other instance
        String registrationMessage = "register " + name + " " + InetAddress.getLocalHost().getHostAddress() + " " + socket.getLocalPort();
        System.out.println(name + " sending registration message: " + registrationMessage);
        buffer = registrationMessage.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, otherAddress, otherPort);
        socket.send(packet);

        // Wait for acknowledgment
        DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(ackPacket);
            String ackMessage = new String(ackPacket.getData(), 0, ackPacket.getLength(), "UTF-8");
            processAckMessage(ackMessage, ackPacket.getAddress(), ackPacket.getPort());
        } catch (IOException e) {
            System.err.println(name + " did not receive acknowledgment: " + e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            String line = readString();
            if (line.equalsIgnoreCase("stop")) break;
            processCommand(line, socket, socket.getLocalPort());
        }
        socket.close();
    }


    private static void processMessage(String message, InetAddress address, int port, DatagramSocket socket) {
        System.out.println("Processing message: " + message + " from " + address.getHostAddress() + ":" + port);
        String[] parts = message.split(" ", 2);
        if (parts[0].equalsIgnoreCase("register")) {
            String[] details = parts[1].split(" ");
            String otherName = details[0];
            String otherIp = details[1];
            int otherPort = Integer.parseInt(details[2]);
            try {
                addressBook.put(otherName, InetAddress.getByName(otherIp));
                portBook.put(otherName, otherPort);
                System.out.println("Registered " + otherName + " with IP " + otherIp + " and port " + otherPort);

                // Send acknowledgment
                String ackMessage = "ack " + name;
                byte[] buffer = ackMessage.getBytes("UTF-8");
                DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(ackPacket);
                System.out.println("Sent acknowledgment to " + address.getHostAddress() + ":" + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Received message: " + message);
        }
    }

    private static void processAckMessage(String message, InetAddress address, int port) {
        System.out.println("Processing ack message: " + message + " from " + address.getHostAddress() + ":" + port);
        String[] parts = message.split(" ", 2);
        if (parts[0].equalsIgnoreCase("ack")) {
            String otherName = parts[1];
            addressBook.put(otherName, address);
            portBook.put(otherName, port);
            System.out.println("Acknowledged by " + otherName + " with IP " + address.getHostAddress() + " and port " + port);
        }
    }

    private static void processCommand(String command, DatagramSocket socket, int port) throws IOException {
        String[] parts = command.split(" ", 3);
        if (parts[0].equalsIgnoreCase("send")) {
            String recipient = parts[1];
            String message = parts[2];
            InetAddress recipientAddress = addressBook.get(recipient);
            Integer recipientPort = portBook.get(recipient);
            if (recipientAddress != null && recipientPort != null) {
                String fullMessage = name + ": " + message;
                byte[] buffer = fullMessage.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, recipientAddress, recipientPort);
                socket.send(packet);
                System.out.println("Message sent to " + recipient + ": " + message);
            } else {
                System.out.println("Recipient not found.");
            }
        }
    }

    private static String readString() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }
}
