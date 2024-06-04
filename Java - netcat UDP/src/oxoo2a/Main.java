package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;

public class Main {


    private static int myPort;
    private static InetAddress myNetAdress;
    private static DatagramSocket mySocket;
    private static final int packetSize = 4096;

    private static HashMap<String, ChatClient> knownConnections = new HashMap<>();

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    // connection registration procedure
    private static void register(String name, InetAddress address, int port) {
        knownConnections.put(name, new ChatClient(name, address, port));
        System.out.println("New connection added: " + name + " " + address + " " + port);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            fatal("Usage: \"<netcat> <port>\"");
        int port = Integer.parseInt(args[0]);
        //get my address from google:
        mySocket = new DatagramSocket(port);
        mySocket.connect(InetAddress.getByName("google.com"), 80);
        myNetAdress = mySocket.getLocalAddress();
        myPort = mySocket.getLocalPort();
        mySocket.disconnect();
        System.out.println("my address: " + myNetAdress + " my port: " + myPort);
        //get my public address:
        URL url = new URL("http://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String publicIpAddress = br.readLine();
        System.out.println("This client can be globally reached under: " + "IP: " + publicIpAddress + "\tPort: " + port);
        //listen for incoming messages:
        new Thread(() -> {
            try {
                listenForMessages();
            } catch (IOException e) {
                mySocket.close();
            }
        }).start();
        //read user input && check for keywords:
        new Thread(() -> {
            try {
                inputInterpreter();
            } catch (IOException e) {
                mySocket.close();
            }
        }).start();

    }
    // ************************************************************************
    // keyword interpreter
    // ************************************************************************

    /**
     * reads user input Valid commands: -"add "name" "ip" "port" -"register "name" -"send "name" "message" -"stop"
     *
     * @throws IOException
     */
    private static void inputInterpreter() throws IOException {
        String line;
        do {
            line = readString();
            //register new connection: "register <name>"
            //used by the client to register itself, for reflecting on himself
            if (line.startsWith("register ")) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    register(parts[1], myNetAdress, myPort);
                }
                //add new contact: "add <name> <ip> <port>"
            } else if (line.startsWith("add ".toLowerCase())) {
                String[] parts = line.split(" ", 4);
                if (parts.length == 4) {
                    //add new contact:
                    register(parts[1], InetAddress.ofLiteral(parts[2]), Integer.parseInt(parts[3]));
                }
                //send message to a specified client by name: "send <name> <message>"
            } else if (line.startsWith("send ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length == 3) {
                    send(parts[1], parts[2]);
                }
            }
        } while (!line.equalsIgnoreCase("stop"));
        //terminates the programm
        mySocket.close();
    }


    // ************************************************************************
    // listen for incoming messages from other clients
    // ************************************************************************
    private static void listenForMessages() throws IOException {
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            mySocket.receive(p);
            line = new String(buffer, 0, p.getLength(), "UTF-8");
            System.out.println(line);
        } while (!mySocket.isClosed());
    }

    // ************************************************************************
    // send message to a specified client by name
    // ************************************************************************
    private static void send(String name, String message) throws IOException {
        //check if the contact is known:
        if (knownConnections.containsKey(name)) {
            byte[] buffer = message.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buffer, buffer.length, knownConnections.get(name).address, knownConnections.get(name).port);
            mySocket.send(p);

        } else {
            System.out.println("unknown Contact, use \"add <name> <ip> <port>\"");
        }
    }
    //used to process user input
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

    // ************************************************************************
    // ChatClient class
    // ************************************************************************
    private static class ChatClient {
        String name;
        InetAddress address;
        int port;

        ChatClient(String name, InetAddress address, int port) {
            this.name = name;
            this.address = address;
            this.port = port;
        }
    }
    // ************************************************************************

}
