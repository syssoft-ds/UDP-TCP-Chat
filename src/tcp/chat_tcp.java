package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class chat_tcp {

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port> <name>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            Server(port);
        else
            Client(args[0],port, args[2]);
    }

    private static HashMap<String, PrintWriter> outputStreams = new HashMap<>();

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server ( int port ) throws IOException {
        ServerSocket s = new ServerSocket(port);
        while (true) {
            Socket client = s.accept();
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient ( Socket clientConnection ) {
        try {
            PrintWriter w = new PrintWriter(clientConnection.getOutputStream(),true);
            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;
            String name = null;
            do {
                line = r.readLine();
                if (line.startsWith("register")) {
                    name = line.substring(9);
                    synchronized (outputStreams) {
                        outputStreams.put(name, w);
                    }
                } else if (line.startsWith("send")) {
                    line = line.substring(5);
                    int sep = line.indexOf(" ");
                    String partner = line.substring(0, sep);
                    String message = line.substring(sep+1);
                    PrintWriter wp;
                    synchronized (outputStreams) {
                        wp = outputStreams.get(partner);
                    }
                    wp.println(name +" wrote: " + message);
                }
                System.out.println(line);
            } while (!line.equalsIgnoreCase("stop"));
            synchronized (outputStreams) {
                outputStreams.remove(name);
            }
            w.println("stop");
            clientConnection.close();

        }
        catch (IOException e) {
            System.out.println("There was an IOException while receiving data ...");
            System.exit(-1);
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client ( String serverHost, int serverPort, String name) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverConnect = new Socket(serverAddress,serverPort);

        new Thread(() -> {
            try {
                PrintWriter w = new PrintWriter(serverConnect.getOutputStream(), true);
                w.println("register " + name);
                String line = "";
                while (true) {
                    line = readString();
                    w.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        BufferedReader r = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            System.out.println(line);
        }
    }

    private static String readString () {
        boolean again = false;
        String input = null;
        do {
            // System.out.print("Input: ");
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private static BufferedReader br = null;
}