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

    private static final int packetSize = 4096;
    private static final Map<String, InetAddress> clientMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");

        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            listenForClients(port);
        else
            connectToServer(args[0], port);
    }

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static void listenForClients(int port) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(receivePacket);

            String message = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
            String[] parts = message.split(" ", 3);
            String command = parts[0];

            if (command.equalsIgnoreCase("register")) {
                String clientName = parts[1];
                InetAddress clientAddress = receivePacket.getAddress();
                clientMap.put(clientName, clientAddress);
                System.out.println(clientName + " connected from " + clientAddress);
            } else if (command.equalsIgnoreCase("send")) {
                String recipientName = parts[1];
                String text = parts[2];
                InetAddress recipientAddress = clientMap.get(recipientName);
                if (recipientAddress != null) {
                    sendMessage(text, recipientAddress, port);
                } else {
                    System.out.println("Empfaenger " + recipientName + " nicht gefunden.");
                }
            }
        }
    }

    private static void sendMessage(String message, InetAddress recipientAddress, int port) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = message.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, recipientAddress, port);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }

    private static void connectToServer(String ipAddress, int port) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(ipAddress);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Was ist dein Name?");
        String clientName = reader.readLine();

        clientMap.put(clientName, InetAddress.getLocalHost());

        while (true) {
            System.out.println("Format: (send 'name' 'message')");
            String input = reader.readLine();
            if (input.equalsIgnoreCase("exit")) break;

            String[] parts = input.split(" ", 3);
            if (parts.length < 3) {
                System.out.println("Bitte befolge das Format (send 'name' 'message')");
                continue;
            }

            String command = parts[0];
            if (!command.equalsIgnoreCase("send")) {
                System.out.println("Commands: 'send'.");
                continue;
            }

            String recipientName = parts[1];
            String text = parts[2];
            sendMessageToServer("send " + recipientName + " " + text, serverAddress, port);
        }
    }

    private static void sendMessageToServer(String message, InetAddress serverAddress, int port) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = message.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
}
