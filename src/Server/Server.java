package Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static int port;
    private static ServerSocket server;


    public Server(int p) {
        port=p;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("An error occurred while starting the server.");
            throw new RuntimeException(e);
        }
        System.out.println("This client can be reached under: " + "\tPort: " + server.getLocalPort());
        listenForConnection();
    }


    public void listenForConnection() {
        while (true) {
            try {
                Socket clientSocket = server.accept();
                HandleClientThread hct = new HandleClientThread(clientSocket);
                hct.start();
            } catch (IOException e) {
                System.out.println("An error occurred during connection.");
            }
        }

    }
}
