package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

public class UDP {

    private static HashMap<String, Peer> peers = new HashMap<>();

    private static class Peer {
        String name;
        InetAddress address;
        int port;

        Peer(String name, InetAddress address, int port) {
            this.name = name;
            this.address = address;
            this.port = port;
        }
    }

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            fatal("Usage: \"<netcat> -l <port> <name>\" or \"netcat <ip> <port> <name>\"");
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port, name);
        else
            connectAndTalk(args[0],port, name);
    }

    private static final int packetSize = 4096;

    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
private static void listenAndTalk ( int port, String name ) throws IOException  {
    DatagramSocket s = new DatagramSocket(port);
    byte[] buffer = new byte[packetSize];
    String line;
    do {
        DatagramPacket p = new DatagramPacket(buffer,buffer.length);
        s.receive(p);
        line = new String(buffer,0,p.getLength(),"UTF-8");
        if (line.startsWith("register ")) {
            String[] parts = line.split(" ");
            if (parts.length == 4) {
                peers.put(parts[1], new Peer(parts[1], InetAddress.getByName(parts[2]), Integer.parseInt(parts[3])));
                // Print a message every time a registration message is received
                System.out.println("Received registration message from " + parts[1]);
                // Print the contents of the peers HashMap
                System.out.println("Peers: " + peers);

                // Broadcast the registration message to all connected clients
                System.out.println("Broadcasting registration message to all connected clients");
                for (Peer peer : peers.values()) {
                    if (!peer.name.equals(parts[1])) { // Don't send the message back to the client that just registered
                        buffer = line.getBytes("UTF-8");
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, peer.address, peer.port);
                        System.out.println("Broadcasting registration message to " + peer.name + " at " + peer.address + ":" + peer.port);
                        System.out.println("DatagramSocket contents:" + s);
                        System.out.println("DatagramPacket contents:" + packet);
                        s.send(packet);
                        System.out.println(s.getLocalPort());
                    }
                }
            }
        } else {
            System.out.println(line);
        }
    } while (!line.equalsIgnoreCase("stop"));
    s.close();
}

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
private static void connectAndTalk ( String other_host, int other_port, String name ) throws IOException {
    InetAddress other_address = InetAddress.getByName(other_host);
    DatagramSocket s = new DatagramSocket();
    byte[] buffer = new byte[packetSize];
    String line;
    do {
        // Print the contents of the peers HashMap
        System.out.println("Peers: " + peers);
        line = readString();
        if (line.startsWith("send ")) {
            System.out.println("sending stuff");
            String[] parts = line.split(" ", 3);
            System.out.println(parts.length);
            System.out.println(Arrays.toString(parts));
            if (parts.length == 3 && peers.containsKey(parts[1])) {
                Peer peer = peers.get(parts[1]);
                buffer = (name + ": " + parts[2]).getBytes("UTF-8");
                System.out.println(Arrays.toString(buffer));
                DatagramPacket p = new DatagramPacket(buffer,buffer.length,peer.address,peer.port);
                s.send(p);
            }

        } else if (line.startsWith("register ")) {
            String[] parts = line.split(" ");
            if (parts.length == 4) {
                peers.put(parts[1], new Peer(parts[1], InetAddress.getByName(parts[2]), Integer.parseInt(parts[3])));
                // Print a message every time a registration message is received
                System.out.println("Received registration message from " + parts[1]);
                // Print the contents of the peers HashMap
                System.out.println("Peers: " + peers);

            }
            buffer = line.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buffer,buffer.length,other_address,other_port);
            s.send(p);
        } else {
            buffer = line.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buffer,buffer.length,other_address,other_port);
            s.send(p);
        }
        // Print a message every time a message is received
        System.out.println("Received message: " + line);
    } while (!line.equalsIgnoreCase("stop"));
    s.close();
}

    private static String readString () {
        BufferedReader br = null;
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private BufferedReader br = null;
}
