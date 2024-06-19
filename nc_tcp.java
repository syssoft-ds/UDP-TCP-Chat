import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class nc_tcp {
    private static BufferedReader br = null;

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
                Server(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("Name: " + name + " Port: " + port + " started.");
        // Main thread continues to process user input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while (!(input = br.readLine()).equalsIgnoreCase("stop")) {
            String[] parts = input.split(" ");
            if (parts[0].equalsIgnoreCase("register") && parts.length == 4) {
                clients.put(parts[3], new ClientInfo(parts[1], Integer.parseInt(parts[2])));
            }
            if (parts[0].equalsIgnoreCase("send")) {
                String name_vom_partner = parts[1];
                ClientInfo partnerInfo = clients.get(name_vom_partner);
                if (partnerInfo != null) {
                    String message = input.substring(input.indexOf(name_vom_partner) + name_vom_partner.length()).trim();
                    Client(partnerInfo.ip, partnerInfo.port, message, name_vom_partner);
                } else {
                    System.out.println("Client " + name_vom_partner + " not found.");
                }
            }
        }
    }

    private static void Server(int port) throws IOException {
        ServerSocket s = new ServerSocket(port);
        while (true) {
            Socket client = s.accept();
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient(Socket clientConnection) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;
            do {
                line = r.readLine();
                if (line == null) break;
                System.out.println(line);
            } while (!line.equalsIgnoreCase("stop"));
            clientConnection.close();
        } catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    private static void Client(String serverHost, int serverPort, String firstMessage, String name_vom_Partner) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverConnect = new Socket(serverAddress, serverPort);
        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);
        String line;
        w.println(name +": " +firstMessage);
        do {
            line = readString();
            String[] parts = line.split(" ");
            if(parts[0].equalsIgnoreCase("register") ||
                    (parts[0].equalsIgnoreCase("send") && !parts[1].equalsIgnoreCase(name_vom_Partner))){
                //falls nachricht an jemanden anderen gesendet wird obwohl letzte verbindung noch offen ist
                System.out.println("Letzte Verbindung wurde geschlossen. Bitte erneut Nachricht an jemanden anderes senden.");
                break;
            }
            w.println(name +": " +line);
        } while (!line.equalsIgnoreCase("stop"));
        serverConnect.close();
    }

    private static String readString() {
        boolean again = false;
        String input = null;
        do {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            } catch (IOException e) {
                System.out.println("There was an IOException while reading data ...");
                again = true;
            }
        } while (again);
        return input;
    }
}