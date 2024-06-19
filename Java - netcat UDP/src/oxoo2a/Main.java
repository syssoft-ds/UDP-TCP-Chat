package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

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

    private static final int packetSize = 4096;

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
            System.out.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk ( String other_host, int other_port ) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            line = readString();
            buffer = line.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buffer,buffer.length,other_address,other_port);
            s.send(p);
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
