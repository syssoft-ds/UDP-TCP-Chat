import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Chat_Client {
    private static String name;
    private static String serverIP;
    private static int serverPort;

    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static boolean isIP(String ip) { // Checks if String is valid IPv4 address
        return ip.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    }

    public static boolean isPort(String port) {
        try {
            int p = Integer.parseInt(port);
            return p > 0 && p <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
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

            System.out.println(name + " is connected to Server at IP " + serverIP + " on port " + serverPort + ".\nUse \"send <client name> <message>\" to send a message to a client.\nUse \"broadcast <message>\" to send a message to all clients.\nUse \"list\" to get the list of all clients.\nUse \"set <question> <answer>\" to set a predefined question and answer.\nUse \"ask <client name> <question>\" to ask a predefined question to a client.");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 3);
                if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    out.println(userInput);
                } else if (parts[0].equalsIgnoreCase("broadcast") && parts.length == 2) {
                    out.println(userInput);
                } else if (parts[0].equalsIgnoreCase("list")) {
                    out.println("list");
                } else if (parts[0].equalsIgnoreCase("set") && parts.length == 3) {
                    out.println(userInput);
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                    out.println(userInput);
                } else {
                    System.err.println("Unknown command.");
                }
            }
        } catch (UnknownHostException e) {
            fatal("Unknown Server with IP " + serverIP);
        } catch (IOException e) {
            fatal("Unable to send message.");
        }
    }
}
