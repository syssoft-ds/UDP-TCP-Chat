
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class tcp_chat {

    private static final String USAGE_MESSAGE = "Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"";
    
    private static void fatalError(String message) {
        System.out.println(message);
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length!= 2) {
            fatalError(USAGE_MESSAGE);
        }
        
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l")) {
            initializeServer(port);
        } else {
            initiateClientConnection(args[0], port);
        }
    }

    private static void initializeServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            processClientRequest(clientSocket);
        }
    }

    private static void processClientRequest(Socket clientSocket) {
        Thread thread = new Thread(() -> handleIncomingData(clientSocket));
        thread.start();
    }

    private static void handleIncomingData(Socket clientConnection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()))) {
            String receivedLine;
            while (!(receivedLine = reader.readLine()).equalsIgnoreCase("stop")) {
                System.out.println(receivedLine);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while processing incoming data...");
            System.exit(-1);
        }
    }

    private static void initiateClientConnection(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            
            String userInput;
            do {
                userInput = getUserInput();
                writer.println(userInput);
            } while (!userInput.equalsIgnoreCase("stop"));
        }
    }

    private static String getUserInput() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            return bufferedReader.readLine();
        } catch (IOException e) {
            System.out.printf("Exception: %s%n", e.getMessage());
            return null; 
        } finally {
            if (bufferedReader!= null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.printf("Failed to close BufferedReader: %s%n", e.getMessage());
                }
            }
        }
    }
}
