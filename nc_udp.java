import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class nc_udp {
    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static String name;
    private static Map<String, InetAddress> nameToAddress = new HashMap<>();
    private static Map<String, Integer> nameToPort = new HashMap<>();

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length < 3)
            fatal("Usage: \"<netcat> -l <name> <port>\" or \"netcat <name> <ip> <port>\"");
        name = args[1];
        int port = Integer.parseInt(args[args.length - 1]);
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else
            connectAndTalk(name, args[1], args[2], port);
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

            if (line.startsWith("register")) {
                String[] parts = line.split(" ");
                String otherName = parts[1];
                InetAddress otherAddress = p.getAddress();
                int otherPort = Integer.parseInt(parts[2]);
                nameToAddress.put(otherName, otherAddress);
                nameToPort.put(otherName, otherPort);
                System.out.printf("Registered %s at %s:%d\n", otherName, otherAddress.getHostAddress(), otherPort);
            } else if (line.startsWith("send")) {
                String[] parts = line.split(" ", 3);
                String targetName = parts[1];
                String message = parts[2];
                if (nameToAddress.containsKey(targetName)) {
                    sendMessage(nameToAddress.get(targetName), nameToPort.get(targetName), message);
                } else {
                    System.out.printf("No known address for %s\n", targetName);
                }
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk(String name, String other_host, String other_name, int other_port) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String line;

        // Register this instance
        String registrationMessage = String.format("register %s %d", name, s.getLocalPort());
        buffer = registrationMessage.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, other_address, other_port);
        s.send(p);

        do {
            line = readString();
            if (line.startsWith("send")) {
                String[] parts = line.split(" ", 3);
                String targetName = parts[1];
                String message = parts[2];
                if (nameToAddress.containsKey(targetName)) {
                    sendMessage(nameToAddress.get(targetName), nameToPort.get(targetName), message);
                } else {
                    System.out.printf("No known address for %s\n", targetName);
                }
            } else {
                buffer = line.getBytes("UTF-8");
                p = new DatagramPacket(buffer, buffer.length, other_address, other_port);
                s.send(p);
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static void sendMessage(InetAddress address, int port, String message) throws IOException {
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = message.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, address, port);
        s.send(p);
        s.close();
    }

    private static String readString() {
        BufferedReader br = null;
        boolean again = false;
        String input = null;
        do {
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            } catch (Exception e) {
                System.out.printf("Exception: %s\n", e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private BufferedReader br = null;
}
