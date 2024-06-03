import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final int packetSize = 4096;
    private static Map<String, InetAddress> registeredClients = new HashMap<>();
    private static Map<String, Integer> registeredPorts = new HashMap<>();
    private static String myName;


    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            listenAndTalk(port);
        else
            connectAndTalk(args[0],port);
    }


    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void listenAndTalk ( int port ) throws IOException  {
        DatagramSocket s = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer,buffer.length);
            s.receive(p);
            line = new String(buffer,0,p.getLength(),"UTF-8");
            handleIncomingMessage(s, p, line);
            System.out.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    private static void handleIncomingMessage(DatagramSocket socket, DatagramPacket packet, String msg) throws IOException{
        String[] parts = msg.split(" ", 3);
        if(parts.length == 3 && parts[0].equals("send")){
            String recipientName = parts[1];
            String msgContent = parts[2];
            if(registeredClients.containsKey(recipientName)){
                InetAddress recipientAdress = registeredClients.get(recipientName);
                int recipientPort = registeredPorts.get(recipientName);
                sendMessage(socket, recipientAdress, recipientPort, myName + ": " + msgContent);
            } else {
                System.out.println("Unknown recipient + recipientName");
            }
        } else if(parts.length == 2 && parts[0].equals("register")){
            String newName = parts[1];
            registeredClients.put(newName, packet.getAddress());
            registeredPorts.put(newName, packet.getPort());
            System.out.println(newName + " registered from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
        } else {
            System.out.println(msg);
        }
    }

    private static void sendMessage(DatagramSocket socket, InetAddress address, int port, String s) throws IOException {
        byte[] buffer = s.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk(String otherHost, int otherPort) throws IOException {
        InetAddress otherAddress = InetAddress.getByName(otherHost);
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        sendMessage(socket, otherAddress, otherPort, "register " + myName);
        String line;
        do {
            line = readString();
            if (line.startsWith("send ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length == 3) {
                    String recipientName = parts[1];
                    String messageContent = parts[2];
                    if (registeredClients.containsKey(recipientName)) {
                        InetAddress recipientAddress = registeredClients.get(recipientName);
                        int recipientPort = registeredPorts.get(recipientName);
                        sendMessage(socket, recipientAddress, recipientPort, myName + ": " + messageContent);
                    } else {
                        System.out.println("Unknown recipient: " + recipientName);
                    }
                } else {
                    System.out.println("Usage: send <name> <message>");
                }
            } else {
                buffer = line.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, otherAddress, otherPort);
                socket.send(packet);
            }
        } while (!line.equalsIgnoreCase("stop"));
        socket.close();
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