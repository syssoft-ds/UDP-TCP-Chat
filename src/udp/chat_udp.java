package udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class chat_udp {

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"chat <port> <name>\"");
        myPort = Integer.parseInt(args[0]);
        myName = args[1];
        DatagramSocket s = new DatagramSocket(myPort);
        new Thread(() -> {
            try{
            listenAndTalk(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        connectAndTalk(s);
    }

    private static final int packetSize = 4096;

    private static String myIp = "127.0.0.1";
    private static int myPort;
    private static String myName;

    private record Endpoint(String ip, int port) {}

    private static HashMap<String, Endpoint> partners = new HashMap<>();


    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void listenAndTalk (DatagramSocket s) throws IOException  {
        s.connect(InetAddress.getByName("8.8.8.8"), 10002);
        myIp = s.getLocalAddress().getHostAddress();
        s.disconnect();
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer,buffer.length);
            s.receive(p);
            line = new String(buffer,0,p.getLength(),"UTF-8");
            if (line.startsWith("register")) {
                boolean toAnswer = line.substring(8,9).equals("1");
                line = line.substring(10);
                int sep = line.indexOf(" ");
                String ip = line.substring(0, sep);
                line = line.substring(sep+1);
                sep = line.indexOf(" ");
                int port = Integer.parseInt(line.substring(0, sep));
                String name = line.substring(sep+1);
                register(toAnswer, name, ip, port, s);
            } else {
                System.out.println(line);
            }
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk (DatagramSocket s) throws IOException {
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            line = readString();
            if (line.startsWith("send")) {
                line = line.substring(5);
                int sep = line.indexOf(" ");
                String name = line.substring(0, sep);
                String message = line.substring(sep+1);
                sendMessage(message, name, s);
            } else if (line.startsWith("register")) {
                line = line.substring(9);
                int sep = line.indexOf(" ");
                String ip = line.substring(0, sep);
                int port = Integer.parseInt(line.substring(sep+1));
                sendRequest(false, ip, port, s);
            } else {
                System.out.println(line);
            }
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


    private static void send (String other_host, int other_port, DatagramSocket s, String message) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        byte[] buffer = message.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer,buffer.length,other_address,other_port);
        s.send(p);
    }

    private static void sendRequest(boolean answering, String otherIp, int otherPort, DatagramSocket s) throws IOException {
        String message = answering ? "register2 " : "register1 ";
        message += myIp + " " + myPort + " " + myName;
        send(otherIp, otherPort, s, message);
    }

    private static void sendMessage(String line, String name, DatagramSocket s) throws IOException {
        String message = myName + " wrote: " + line;
        Endpoint ep = partners.get(name);
        send(ep.ip, ep.port, s, message);
    }

    private static void register(boolean toAnswer, String name, String otherIp, int otherPort, DatagramSocket s) throws IOException {
        partners.put(name, new Endpoint(otherIp, otherPort));
        System.out.println("registered " + name);
        if (toAnswer) {
            sendRequest(true, otherIp, otherPort, s);
        }
    }



    private BufferedReader br = null;
}