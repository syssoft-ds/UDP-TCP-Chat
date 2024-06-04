import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// Auch hier hat beim Verstehen und auch ein wenig beim Schreiben ein KI-Assistent geholfen.
public class TCPChat {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static Map<String, Socket> clients = new HashMap<>();

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            Server(port);
        else
            Client(args[0], port);
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server auf Port " + port + " gestartet!");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread clientThread = new Thread(() -> bearbeiteClientAnfragen(clientSocket));
            clientThread.start();
        }
    }

    private static void bearbeiteClientAnfragen(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            writer.println("Gib deinen Namen an:");
            String name = reader.readLine();
            synchronized (clients) {
                clients.put(name, clientSocket);
            }
            writer.println("Hallo, " + name + "! Mit \"send <name> <message>\" kannst du jetzt jedem anderen Client <name> auf dem Server Nachrichten <message> schicken!");

            String ganzeNachricht;
            while ((ganzeNachricht = reader.readLine()) != null) {
                if (ganzeNachricht.equalsIgnoreCase("stop")) {
                    break;
                } else if (ganzeNachricht.startsWith("send ")) {
                    String[] nachrichtenteil = ganzeNachricht.split(" ", 3);
                    if (nachrichtenteil.length >= 3) {
                        String empfaenger = nachrichtenteil[1];
                        String textnachricht = nachrichtenteil[2];
                        versendeNachricht(name, empfaenger, textnachricht, writer);
                    } else {
                        writer.println("Nutze \"send <name> <message>\" um Nachrichten zu verschicken.");
                    }
                }
            }
            synchronized (clients) {
                clients.remove(name);
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: Fehler beim schreiben oder lesen!");
            e.printStackTrace();
        }
    }

    private static void versendeNachricht(String sender, String empfaenger, String textnachricht, PrintWriter senderWriter) {
        Socket recipientSocket;
        synchronized (clients) {
            recipientSocket = clients.get(empfaenger);
        }
        if (recipientSocket != null) {
            try {
                PrintWriter empfaengerWriter = new PrintWriter(recipientSocket.getOutputStream(), true);
                empfaengerWriter.println("Nachricht von " + sender + ": " + textnachricht);
            } catch (IOException e) {
                senderWriter.println("IOException: Nachricht konnte nicht geschrieben werden!");
                e.printStackTrace();
            }
        } else {
            senderWriter.println(empfaenger + " ist nicht mit dem Server verbunden.");
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client(String serverHost, int serverPort) throws IOException {
        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverSocket = new Socket(serverAddress, serverPort);
        PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

        String ganzeNachricht;
        new Thread(() -> {
            try {
                String serverNachricht;
                while ((serverNachricht = reader.readLine()) != null) {
                    System.out.println(serverNachricht);
                }
            } catch (IOException e) {
                System.out.println("IOException: Fehler beim lesen der Server Nachricht!");
                e.printStackTrace();
            }
        }).start();

        while ((ganzeNachricht = readString()) != null) {
            writer.println(ganzeNachricht);
            if (ganzeNachricht.equalsIgnoreCase("stop")) {
                break;
            }
        }
        serverSocket.close();
    }

    private static String readString() {
        boolean again;
        String input = null;
        do {
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
                again = false;
            } catch (Exception e) {
                System.out.printf("Exception: %s\n", e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private static BufferedReader br = null;
}

