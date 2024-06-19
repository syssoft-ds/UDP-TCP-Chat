import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPChatClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java TCPChatClient <server_ip> <server_port>");
            return;
        }

        String serverIp = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            // Read and print the welcome message from the server
            System.out.println(in.readLine());

            // Send the client's name to the server
            String clientName = scanner.nextLine();
            out.println(clientName);

            // Read and print the registration confirmation from the server
            System.out.println(in.readLine());

            // Start a new thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Read and send messages to the server
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                out.println(message);
                if (message.equalsIgnoreCase("stop")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
