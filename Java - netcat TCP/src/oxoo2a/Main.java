package oxoo2a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;


public class Main {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static ServerSocket serverSocket;
    private static HashMap<String, ChatClient> knownConnections = new HashMap<>();

    // connection registration procedure
    private static void register(String name, Socket givenSocket) {
        //duplicates prevention
        for (ChatClient client: knownConnections.values()){
            if(client.clientSocket.equals(givenSocket)){
                System.out.println("This connection is already registered under the name: "+client.name);
                return;
            }
        }
        knownConnections.put(name, new ChatClient(name, givenSocket));
        System.out.println("New connection added: " + name + " " + givenSocket.getInetAddress() + " " + givenSocket.getPort());
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port> <name>\"");
        int port = Integer.parseInt(args[1]);
        //get my public address:
        URL url = new URL("http://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        String publicIpAddress = br.readLine();
        System.out.println("This client can be globally reached under: " + "IP: " + publicIpAddress + "\tPort: " + port);
        //end of ip validation
        if (args[0].equalsIgnoreCase("-l"))
            new Thread(() -> {
                try {
                    Server(port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        else {
            new Thread(() -> {
                try {
                    Client(args[0], port, args[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            Socket client = serverSocket.accept();
            //create a new thread for each client
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient(Socket clientConnection) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;
            do {
                line = r.readLine();
                //interpret input line:
                if (line.startsWith("register ".toLowerCase())) {
                    String[] parts=line.split(" ", 2);
                    if(parts.length==2){
                        register(parts[1],clientConnection);
                    }
                }
                //client to send message to a specified client by name: "send <name> <message>"
                //client sends to server and server sends to destination client
                else if (line.startsWith("send ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length == 3) {
                        if(knownConnections.containsKey(parts[1])){
                            ChatClient destination = knownConnections.get(parts[1]);
                            try{
                                System.out.println("test");
                                PrintWriter w = new PrintWriter(clientConnection.getOutputStream(), true);
                                w.println(parts[2]);
                                w.flush();
                            }catch (IOException e){
                                System.out.println("There was an IOException while sending data ...\n" +
                                        "details: line= "+line +"\n destination socket= "+ destination.clientSocket+"\n");
                                System.exit(-1);
                            }
                        }
                    }
                }
                System.out.println(line);
            } while (!line.equalsIgnoreCase("stop"));
            clientConnection.close();
        } catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client(String serverHost, int serverPort, String clientName) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        //serverConnect is the connection to the server
        Socket serverConnect = new Socket(serverAddress, serverPort);
        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);//let the server deal with interpretation of the clients input
        //register the client with the server immediately after connecting
        w.println("register " + clientName);
        w.flush();
        String userLine;
        //listen for incoming messages from the server, marked with "->"
        new Thread(()->{
        String serverInputLine;
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            do{
                try {
                    serverInputLine = r.readLine();
                    System.out.println("-> "+serverInputLine);
                }catch (IOException e){
                    System.out.println("Server connection error");
                    System.exit(-1);
                }

            } while(!serverConnect.isClosed()&&serverConnect.isConnected());
        }).start();
        //transmit user input to the server & terminate the connection if the user types "stop"
        do {
            userLine = readString();
            w.println(userLine);
        } while (!userLine.equalsIgnoreCase("stop"));
        serverConnect.close();
    }

    private static String readString() {
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            } catch (Exception e) {
                System.out.printf("Exception: %s\n", e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private static BufferedReader br = null;

    // ************************************************************************
    // ChatClient class
    // ************************************************************************
    private static class ChatClient {
        String name;
        InetAddress address;
        int port;
        Socket clientSocket;

        ChatClient(String name, Socket clientSocket) {
            this.name = name;
            this.address = clientSocket.getInetAddress();
            this.port = clientSocket.getPort();
            this.clientSocket = clientSocket;
        }
    }
    // ************************************************************************
}
