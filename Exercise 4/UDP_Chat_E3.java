import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDP_Chat_E3{


    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port> \" or \"netcat <ip> <port>\"");
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
        byte[] buffer2 = new byte[packetSize];
        Map<String, Data> users = new HashMap<>();
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer,buffer.length);
            s.receive(p);
            line = new String(buffer,0,p.getLength(),"UTF-8");
            if (line.startsWith("register")){
                System.out.println(line);
                String[] parts = line.split(" ");
                String other_name = parts[1];
                System.out.println("Packetstuff: " + p.getAddress() + " " + p.getPort());
                users.put(other_name, new Data(p.getAddress(), p.getPort()));
                line = "registered " + other_name ;
            }
            if (line.startsWith("send")){
                String[] parts = line.split(" ");
                String user = parts[1];
                String message = line.substring(parts[0].length() + parts[1].length() + 2);
                Data user_info = users.get(user);
                buffer2 = message.getBytes();
                DatagramPacket p2 = new DatagramPacket(buffer2,buffer2.length,user_info.address,user_info.port);
                s.send(p2);
            }
            System.out.println(line);
        } while (!line.equalsIgnoreCase("stop"));
        s.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk ( String other_host, int other_port) throws IOException {
        InetAddress other_address = InetAddress.getByName(other_host);
        System.out.println(other_address);
        DatagramSocket s = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String line;

        ReceiveThread receiveThread = new ReceiveThread(s, buffer);
        receiveThread.start();

        do {
            line = readString();
            buffer = line.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buffer,buffer.length,other_address,other_port);
            s.send(p);
        } while (!line.equalsIgnoreCase("stop"));
        receiveThread.interrupt();
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

class Data{
    InetAddress address;
    int port;

    public Data(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }
}

class ReceiveThread extends Thread {
    private DatagramSocket socket;
    private byte[] buffer;

    public ReceiveThread(DatagramSocket socket, byte[] buffer) {
        this.socket = socket;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socket.receive(p);
                String line = new String(buffer, 0, p.getLength(), "UTF-8");
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}