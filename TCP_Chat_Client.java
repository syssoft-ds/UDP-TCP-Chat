//funktionirt nicht vollständig
import java.io.*;
import java.net.*;

public class TCP_Chat_Client {
    private static String name = "Client1";
    private static String serverIP = "231.255.194.236";
    private static int serverPort = 1444;

    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static void sendToAll(PrintWriter out, String message) {
        out.println("send all " + message);
        System.out.println("Message sent to all clients.");
    }

    public static void askQuestion(PrintWriter out, String clientName, String question) {
        out.println("ask " + clientName + " " + question);
        System.out.println("Question sent to " + clientName + ".");
    }

    public static void setPredefinedAnswer(PrintWriter out, String question, String answer) {
        out.println("set " + question + " " + answer);
        System.out.println("Predefined answer set for question: " + question);
    }

    public static boolean isIP(String ip) { // Checks if String is valid IPv4 address
        return UDP_Chat.isIP(ip);
    }

    public static boolean isPort(String port) {
        return UDP_Chat.isPort(port);
    }

    public static void getClientList(PrintWriter out) {
        out.println("get clientlist");
        System.out.println("Requesting server to send client list.");
    }

    public static void main(String[] args) {

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

            System.out.println(name + " is connected to Server at IP " + serverIP + " on port " + serverPort + ".\nUse \"send <client name> <message>\" to send a message to a client.\nUse \"send all <message>\" to send a message to all clients.\nUse \"ask <client name> <question>\" to ask a question to a specific client.\nUse \"set <question> <answer>\" to set a predefined answer for a question.");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 3);
                if (parts[0].equalsIgnoreCase("send") && parts.length >= 3) {
                    if (parts[1].equalsIgnoreCase("all")) {
                        sendToAll(out, parts[2]);
                    } else {
                        out.println(userInput);
                        System.out.println("Message sent to " + parts[1] + ".");
                    }
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length >= 3) {
                    askQuestion(out, parts[1], parts[2]);
                } else if (parts[0].equalsIgnoreCase("set") && parts.length >= 3) {
                    setPredefinedAnswer(out, parts[1], parts[2]);
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

