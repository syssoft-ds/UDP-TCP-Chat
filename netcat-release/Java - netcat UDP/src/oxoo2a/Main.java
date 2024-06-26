package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class Main {
    static HashMap<String, InetSocketAddress> users = new HashMap<>();
    static String myUsername;

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            fatal("Usage: \"<netcat> -l <port> <username>\" or \"netcat <ip> <port> <username>\"");
        int port = Integer.parseInt(args[1]);
        myUsername = args[2];
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else
            connectAndTalk(args[0], port, myUsername);
    }

    private static final int packetSize = 4096;

    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void listenAndTalk(int port) throws IOException {
        DatagramSocket s = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            s.receive(p);
            line = new String(buffer, 0, p.getLength(), "UTF-8");
            System.out.println(line);

            // Check if it's a registration message
            if (line.startsWith("Hallo")) {
                String[] parts = line.split(" ");
                String username = parts[4];
                InetSocketAddress address = new InetSocketAddress(p.getAddress(), p.getPort());
                users.put(username, address);
                System.out.println(username + " registriert von " + p.getAddress().getHostAddress() + ":" + p.getPort());
            }

        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk(String other_host, int other_port, String username) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();

        // Send registration message
        String registerMessage = "Hallo, hier ist " + username + ", meine IP-Adresse ist " + InetAddress.getLocalHost().getHostAddress() + " und du kannst mich unter Port-Nummer " + s.getLocalPort() + " erreichen.";
        byte[] registerBuffer = registerMessage.getBytes("UTF-8");
        DatagramPacket registerPacket = new DatagramPacket(registerBuffer, registerBuffer.length, other_address, other_port);
        s.send(registerPacket);

        byte[] buffer = new byte[packetSize];
        String line;
        do {
            line = readString();
            if (line.startsWith("send")) {
                String[] parts = line.split(" ", 3);
                String targetName = parts[1];
                String message = parts[2];
                InetSocketAddress targetAddress = users.get(targetName);
                if (targetAddress != null) {
                    buffer = message.getBytes("UTF-8");
                    DatagramPacket p = new DatagramPacket(buffer, buffer.length, targetAddress.getAddress(), targetAddress.getPort());
                    s.send(p);
                } else {
                    System.out.println("Benutzer " + targetName + " nicht gefunden.");
                }
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
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
