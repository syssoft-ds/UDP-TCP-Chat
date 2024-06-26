import java.io.*;
import java.net.*;
import java.util.Scanner;

public class nc_tcp {

    public static void server(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started, waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> serveClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serveClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Message <" + line + "> received from client " + clientSocket.getRemoteSocketAddress());
                if (line.equalsIgnoreCase("stop")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void client(String host, int port) {
        try (Socket clientSocket = new Socket(host, port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected to the server. Type your messages:");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                out.println(line);
                if (line.equalsIgnoreCase("stop")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: \"SocketProgram -l <port>\" or \"SocketProgram <ip> <port>\"");
            System.exit(1);
        }

        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l")) {
            server(port);
        } else {
            client(args[0], port);
        }
    }
}
