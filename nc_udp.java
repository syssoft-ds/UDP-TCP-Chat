import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class nc_udp {

    static int port;
    static String name;
    private static Map<String, ClientInfo> clients = new HashMap<>();

    private static class ClientInfo {
        String ip;
        int port;

        ClientInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            fatal("Usage: \"<netcat> start <port> <eigener name>\"");
        port = Integer.parseInt(args[1]);
        name = args[2];
        // Start a new thread to listen for messages
        new Thread(() -> {
            try {
                receiveLines(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Main thread continues to process user input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while (!(input = br.readLine()).equalsIgnoreCase("stop")) {
            String[] parts = input.split(" ");
            if (parts[0].equalsIgnoreCase("register") && parts.length == 3) {
                register(parts[1], Integer.parseInt(parts[2]));
            }
            if (parts[0].equalsIgnoreCase("send")) {
                String name_vom_partner = parts[1];
                ClientInfo partnerInfo = clients.get(name_vom_partner);
                if (partnerInfo != null) {
                    String message = input.substring(input.indexOf(name_vom_partner) + name_vom_partner.length()).trim();
                    sendLines(partnerInfo.ip, partnerInfo.port, message);
                } else {
                    System.out.println("Client " + name_vom_partner + " not found.");
                }
            }
        }
    }

    private static final int packetSize = 4096;

    private static void receiveLines(int port) throws IOException {
        DatagramSocket s = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            s.receive(p);
            line = new String(buffer, 0, p.getLength(), "UTF-8");
            if (line.startsWith("Hallo, hier ist ")) {
                String[] parts = line.split(", ");
                String name = parts[1].split(" ")[2];
                String ip = parts[2].split(" ")[4];
                int clientPort = Integer.parseInt(parts[2].split(" ")[11]);
                clients.put(name, new ClientInfo(ip, clientPort));
            }
            System.out.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static void sendLines(String other_host, int other_port, String message) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = message.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer, buffer.length, other_address, other_port);
        s.send(p);
        s.close();
    }

    private static void register(String other_host, int other_port) throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String message = String.format("Hallo, hier ist %s, meine IP-Adresse ist die %s " +
                "und du kannst mich unter Port-Nummer %d erreichen.", name, ip, port);
        sendLines(other_host, other_port, message);
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
}