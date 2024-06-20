package aufgabe2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URL;

public class netcatUDP {

    private static final int packetSize = 4096;
    private static int port;;
    private static SocketAddress lastReceivedConnection = null;

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args){
        if (args.length != 1)
            fatal("netcatUDP usage: \"<netcat> <port>\"");
        port = Integer.parseInt(args[0]);
        try {
            startThreads();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }

    // ************************************************************************
    // startThreads
    // ************************************************************************
    private static void startThreads() throws  IOException {
        DatagramSocket socket = new DatagramSocket(port);

        // Ausgabe der Verbindungsinformationen
        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
        String ip = socket.getLocalAddress().getHostAddress();
        socket.disconnect();
        System.out.println("This client can be locally reached under: " + "IP: " + ip + "\tPort: " + port);

        URL url = new URL("http://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String publicIpAddress = br.readLine(); //you get the IP as a String
        System.out.println("This client can be globally reached under: " + "IP: " + publicIpAddress + "\tPort: " + port);



        new ReceiverThread(socket).start();
        new CommandThread(socket).start();
    }

    // ************************************************************************
    // getter/setter methods
    // ************************************************************************

    public static synchronized void setLastReceivedConnection(SocketAddress sa){
        lastReceivedConnection = sa;
    }

    public static synchronized SocketAddress getLastReceivedConnection(){
        return lastReceivedConnection;
    }

}
