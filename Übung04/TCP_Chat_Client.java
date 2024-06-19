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
        return UDP_Chat.isIP(ip);
    }

    public static boolean isPort(String port) {
        return UDP_Chat.isPort(port);
    }

    public static void main(String[] args) {

        // Handling arguments, checking validity
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
            // closes automatically

            new Thread(() -> {
                try {
                    String fromServer;
                    String[] parts;
                    while ((fromServer = in.readLine()) != null) {
                        parts = fromServer.split(" ", 5);
                        if (fromServer.equals("ping")) {
                            System.out.println("ping received. sending pong");
                            out.println("pong");
                        } else if(parts.length == 5 && parts[3].equalsIgnoreCase(":") && parts[4].equalsIgnoreCase("frühstück?")){
                            out.println("send " + parts[2] + " Leberwurscht");
                            System.out.println("send " + parts[2] + " Leberwurscht");
                        } else {
                            System.out.println(fromServer);
                        }
                    }
                } catch (IOException e) {
                    fatal("Unable to get message from Server.");
                }
            }).start();

            // Register the client with the server
            send(out, "register " + name, name + " is connected to Server at IP " + serverIP + " on port " + serverPort + ".\nUse \"send <client name> <message>\" to send a message to a client.");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 3);
                if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    send(out, userInput, "Message sent.");
                } else if (parts[0].equalsIgnoreCase("sendall") && parts.length == 2){
                    send(out, userInput, "Message sent to all clients.");
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

    private static void send(PrintWriter out, String userInput, String x) {
        out.println(userInput);
        System.out.println(x);
    }
}