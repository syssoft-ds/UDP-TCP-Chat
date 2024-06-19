package oxoo2a.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class TCP_Chat_Client {
    private static String name;
    private static String serverIP;
    private static int serverPort;

    private static void fatal(String input) {
        System.err.println(input);
        System.exit(-1);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            fatal("Arguments: \"<server ip address> <server port number> <client name>\"");
        }

        serverIP = args[0];
        serverPort = Integer.parseInt(args[1]);
        name = args[2];

        try (Socket socket = new Socket(serverIP, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        if (fromServer.startsWith("ask ")) {
                            String[] parts = fromServer.split(" ", 3);
                            String asker = parts[1];
                            String question = parts[2];
                            handlePredefinedQuestion(question, out, asker);
                        } else {
                            System.out.println(fromServer);
                        }
                    }
                } catch (IOException e) {
                    fatal("Unable to get message from Server.");
                }
            }).start();

            // Register the client with the server
            out.println("register " + name);

            // Main thread to handle user input
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                String[] parts = userInput.split(" ", 3);
                if (parts[0].equalsIgnoreCase("send") && parts.length == 3) {
                    out.println(userInput);
                    System.out.println("Message sent.");
                } else if (parts[0].equalsIgnoreCase("sendall")) {
                    out.println("sendall " + userInput.substring(8)); // Send the message to server
                    System.out.println("Message sent to all clients.");
                } else if (parts[0].equalsIgnoreCase("ask") && parts.length == 3) {
                    out.println(userInput); // Send ask command to server
                    System.out.println("Request sent.");
                } else {
                    System.err.println("Unknown command.");
                }
            }
        } catch (UnknownHostException e) {
            fatal("Unknown Server with IP " + serverIP);
        } catch (IOException e) {
            fatal("Unable to send/receive message.");
        }
        System.exit(0);
    }

    private static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macAddress = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "MAC-Adresse nicht gefunden";
    }

    private static void handlePredefinedQuestion(String question, PrintWriter out, String asker) {
        if (question.equalsIgnoreCase("Was ist deine MAC-Adresse?")) {
            out.println("response " + asker + " " + getMacAddress());
        } else if (question.equalsIgnoreCase("Sind Kartoffeln eine richtige Mahlzeit?")) {
            out.println("response " + asker + " Ja, Kartoffeln sind eine richtige Mahlzeit.");
        } else {
            out.println("response " + asker + " Unbekannte Frage.");
        }
    }
}
