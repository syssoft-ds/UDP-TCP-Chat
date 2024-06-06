package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.out.println("Exiting ...");
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <my port> <server ip> <server port>\"");
        if (args[0].equalsIgnoreCase("-l"))
            Server(Integer.parseInt(args[1]));
        else
            Client(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server(int port) throws IOException {
        ServerSocket s = new ServerSocket(port);

        Map<String, InetAddress> ips = new HashMap<>();
        Map<String, Integer> ports = new HashMap<>();
        while (true) {
            Socket client = s.accept();
            serveClient(client, ips, ports);
        }
    }

    private static void serveClient(Socket clientConnection, Map<String, InetAddress> ips, Map<String, Integer> ports) {
        try {
            clientConnection.getInetAddress();
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;
            {
                line = r.readLine();
                System.out.println(line);
                if (line.startsWith("send ")) {
                    // send <name> <message>
                    String[] body = line.substring(5).split(" ", 2);
                    Socket serverConnect = new Socket(ips.get(body[0]), ports.get(body[0]));
                    PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);
                    w.println(body[1]);
                    serverConnect.close();

                } else if (line.startsWith("new ")) {
                    // new <name> <ip> <port>
                    // Welcome message broadcast to all known contacts
                    String[] body = line.substring(4).split(" ");
                    for (String key : ips.keySet()) {
                        String message = "Hallo, hier ist " + body[0] + ", meine IP-Adresse ist die " + body[1]
                                + " und du kannst mich unter Port-Nummer " + body[2] + " erreichen.";

                        Socket serverConnect = new Socket(ips.get(key), ports.get(key));
                        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);
                        w.println(message);
                        serverConnect.close();
                    }

                    ips.put(body[0], InetAddress.getByName(body[1]));
                    ports.put(body[0], Integer.parseInt(body[2]));
                }
            }
            clientConnection.close();
        } catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client(int myport, String serverHost, int serverPort) throws IOException {
        Thread t = new Thread(() -> listen(myport));
        t.start();

        InetAddress serverAddress = InetAddress.getByName(serverHost);
        String line;
        while (true) {
            line = readString();
            if (line.equals("exit"))
                break;
            Socket serverConnect = new Socket(serverAddress, serverPort);
            PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);
            w.println(line);
            serverConnect.close();
        }
    }

    private static void listen(int port) {
        try {
            ServerSocket s;
            s = new ServerSocket(port);
            while (true) {
                Socket server = s.accept();
                BufferedReader r = new BufferedReader(new InputStreamReader(server.getInputStream()));
                String line;
                line = r.readLine();
                System.out.println(line);
                server.close();
            }
        } catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    private static String readString() {
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

    private static BufferedReader br = null;
}
