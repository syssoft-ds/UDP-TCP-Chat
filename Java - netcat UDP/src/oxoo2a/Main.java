package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"netcat <name> <port>\"");
        String name = args[0];
        int myport = Integer.parseInt(args[1]);
        String myip = getIP();

        // Receive thread
        Thread receiveThread = new Thread(() -> {
            try {
                DatagramSocket rs = new DatagramSocket(myport);
                byte[] rbuffer = new byte[packetSize];
                DatagramPacket rp = new DatagramPacket(rbuffer, rbuffer.length);
                rs.receive(rp);
                String rline = new String(rbuffer, 0, rp.getLength(), "UTF-8");
                System.out.println(rline);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        // Sending
        DatagramSocket s = new DatagramSocket(myport + 1);
        byte[] buffer = new byte[packetSize];
        String line;
        Map<String, InetAddress> ips = new HashMap<>();
        Map<String, Integer> ports = new HashMap<>();

        // Sending an adding new contacts
        do {
            String message;
            line = readString();
            if (line.startsWith("send ")) {
                // send <name> <message>
                String[] body = line.substring(5).split(" ", 2);
                message = name + ": " + body[1];
                buffer = message.getBytes("UTF-8");
                DatagramPacket sp = new DatagramPacket(buffer, buffer.length, ips.get(body[0]), ports.get(body[0]));
                s.send(sp);
            } else if (line.startsWith("add ")) {
                // add <name> <ip> <port>
                String[] body = line.substring(4).split(" ", 3);
                ips.put(body[0], InetAddress.getByName(body[1]));
                ports.put(body[0], Integer.parseInt(body[2]));

                // Welcome message
                message = "Hallo, hier ist " + name + ", meine IP-Adresse ist die " + myip
                        + " und du kannst mich unter Port-Nummer " + myport + " erreichen.";
                buffer = message.getBytes("UTF-8");
                DatagramPacket wp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(body[1]),
                        Integer.parseInt(body[2]));
                s.send(wp);
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static final int packetSize = 4096;

    // Code extract from
    // https://stackoverflow.com/questions/8083479/java-getting-my-ip-address
    // by user Error404
    private static String getIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    final String ip = addr.getHostAddress();
                    if (Inet4Address.class == addr.getClass())
                        return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static String readString() {
        BufferedReader br = null;
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
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
