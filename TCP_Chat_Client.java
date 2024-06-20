import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class TCP_Chat_Client {
    private static String name;
    private static String serverIP;
    private static int serverPort;
    private static Set<String> knownClients = new HashSet<>();

    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static boolean isIP(String ip) {
        return UDP_Chat.isIP(ip);
    }

    public static boolean isPort(String port) {
        return UDP_Chat.isPort(port);
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            fatal("Arguments: \"<server ip address> <server port number> <client name>\"");
        }
        if (!isIP(args[0])) {
            fatal("Invalid IP address");
        } else {
            serverIP = args[0];
        }
        if (!isPort(args[1])) {
            fatal("Invalid port number");
        } else {
            serverPort = Integer.parseInt(args[1]);
        }
        name = args[2];

        try (Socket socket = new Socket(serverIP, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    fatal("Unable to get message from Server.");
                }
            }).start();

            out.println("register " + name);

            System.out.println(name + " is connected to Server at IP " + serverIP + " on port " + serverPort + ".\nUse \"send <client name> <message>\" to send a message to a client.");
            System.out.println("Use \"broadcast <message>\" to send a message to all clients.");
            System.out.println("Use \"getclients\" to get the list of known clients.");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 2);
                if (parts[0].equalsIgnoreCase("send") && parts.length == 2) {
                    out.println(userInput);
                    System.out.println("Message sent.");
                } else if (parts[0].equalsIgnoreCase("broadcast") && parts.length == 2) {
                    out.println("broadcast " + parts[1]);
                    System.out.println("Broadcast message sent.");
                } else if (parts[0].equalsIgnoreCase("getclients")) {
                        out.println("getclients");
                        System.out.println("Requested client list from server.");
                } else {
                    System.err.println("Unknown command.");
                }
            }
        } catch (UnknownHostException e) {
            fatal("Unknown Server with IP " + serverIP);
        } catch (IOException e) {
            fatal("Unable to send message.");
        }
        System.exit(0);
    }

    private static void updateClientList(String clientList){
        String[] clients = clientList.split(",");
        knownClients.clear();
        for(String client : clients){
            knownClients.add(client.trim());
        }
        System.out.println("Updated ClientList: " + knownClients);
    }
}
