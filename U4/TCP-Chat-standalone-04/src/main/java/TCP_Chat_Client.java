import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class TCP_Chat_Client {
    private static String name;
    private static String serverIP;
    private static int serverPort;
    private static final Map<String, String> predefinedAnswers = new HashMap<>();

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
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)))
        {
            // closes automatically

            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                        if (fromServer.equals("ping")) {
                            out.println("pong");
                        }else if (fromServer.startsWith("clientlist ")) {
                            System.out.println("Active clients: " + fromServer.substring(11));
                        }else if (fromServer.startsWith("question ")) {
                            String question = fromServer.substring(9);
                            String answer = predefinedAnswers.get(question);
                            if (answer != null) {
                                out.println("answer " + answer);
                            }else {
                                out.println("ask server what is the answer to all questions?");
                            }
                        }
                    }
                } catch (IOException e) {
                    fatal("Unable to get message from Server.");
                }
            }).start();

            // Register the client with the server
            out.println("register " + name);

            System.out.println(name + " is connected to Server at IP " + serverIP + " on port " + serverPort +
                    ".\nUse \"send <client name> <message>\" to send a message to a client.");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 3);


                if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    out.println(userInput);
                    System.out.println("Message sent.");
                    //set predefined answer to predefined question
                }else if (parts[0].equalsIgnoreCase("set") && parts.length == 3) {
                    String question = parts[1];
                    String answer = parts[2];
                    predefinedAnswers.put(question, answer);
                    System.out.println("Predefined answer set.");
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
}
