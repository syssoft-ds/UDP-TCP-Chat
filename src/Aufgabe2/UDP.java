package Aufgabe2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UDP {
    private String name;
    private int port;
    private Map<String, Contact> contacts = new HashMap<>();
    private DatagramSocket socket;

    public UDP(String name, int port) {
        this.name = name;
        this.port = port;
        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(String contactName, String contactIp, int contactPort) {
        String message = String.format("Hello, this is %s, my IP is %s and you can reach me on port %d.",
                name, getLocalIpAddress(), port);
        sendMessage(contactIp, contactPort, message);
        contacts.put(contactName, new Contact(contactIp, contactPort));
    }

    public void sendMessage(String contactName, String message) {
        if (contacts.containsKey(contactName)) {
            Contact contact = contacts.get(contactName);
            sendMessage(contact.ip, contact.port, name + ": " + message);
        } else {
            System.out.println("Contact not found: " + contactName);
        }
    }

    private void sendMessage(String ip, int port, String message) {
        try {
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + received);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    private static class Contact {
        String ip;
        int port;

        Contact(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java UDPChat <name> <port>");
            return;
        }

        String name = args[0];
        int port = Integer.parseInt(args[1]);
        UDP chat = new UDP(name, port);

        Thread listenerThread = new Thread(() -> chat.listen());
        listenerThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command (register/send/exit): ");
            String command = scanner.nextLine();
            if (command.equalsIgnoreCase("exit")) {
                break;
            } else if (command.startsWith("register")) {
                String[] parts = command.split(" ");
                if (parts.length != 4) {
                    System.out.println("Usage: register <name> <ip> <port>");
                    continue;
                }
                String contactName = parts[1];
                String contactIp = parts[2];
                int contactPort = Integer.parseInt(parts[3]);
                chat.register(contactName, contactIp, contactPort);
            } else if (command.startsWith("send")) {
                String[] parts = command.split(" ", 3);
                if (parts.length != 3) {
                    System.out.println("Usage: send <name> <message>");
                    continue;
                }
                String contactName = parts[1];
                String message = parts[2];
                chat.sendMessage(contactName, message);
            } else {
                System.out.println("Unknown command: " + command);
            }
        }

        scanner.close();
        System.exit(0);
    }
}