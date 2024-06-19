import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Auch hier hat beim Verstehen und auch ein wenig beim Schreiben ein KI-Assistent geholfen.
public class TCPChat {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static Map<String, Socket> clients = new ConcurrentHashMap<>();
    private static Map<String, String> predefinedAnswers = new ConcurrentHashMap<>();

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
            writer.println("Mit \"send all <message>\" kannst du eine Nachricht an alle schicken.");
            writer.println("Mit \"Getclientlist\" kannst du die Liste aller verbundenen Clients anfordern.");
            writer.println("Mit \"ask <name> <Frage>\" kannst du vordefinierte Fragen stellen.");
            writer.println("Mit \"set <Frage> <Antwort>\" kannst du eine vordefinierte Antwort setzen.");

            String ganzeNachricht;
            while ((ganzeNachricht = reader.readLine()) != null) {
                if (ganzeNachricht.equalsIgnoreCase("stop")) {
                    break;
                } else if (ganzeNachricht.startsWith("send ")) {
                    String[] nachrichtenteil = ganzeNachricht.split(" ", 3);
                    if (nachrichtenteil.length >= 3) {
                        String empfaenger = nachrichtenteil[1];
                        String textnachricht = nachrichtenteil[2];
                        if (empfaenger.equalsIgnoreCase("all")) {
                            versendeNachrichtAnAlle(name, textnachricht);
                        } else {
                            versendeNachricht(name, empfaenger, textnachricht, writer);
                        }
                    } else {
                        writer.println("Nutze \"send <name> <message>\" oder \"send all <message>\" um Nachrichten zu verschicken.");
                    }
                } else if (ganzeNachricht.equalsIgnoreCase("Getclientlist")) {
                    checkInactiveClients();
                    sendeClientListe(writer);
                } else if (ganzeNachricht.startsWith("ask ")) {
                    String[] frageTeil = ganzeNachricht.split(" ", 3);
                    if (frageTeil.length >= 3) {
                        String empfaenger = frageTeil[1];
                        String frage = frageTeil[2];
                        sendeFrage(name, empfaenger, frage, writer);
                    } else {
                        writer.println("Nutze \"ask <name> <Frage>\" um vordefinierte Fragen zu stellen.");
                    }
                } else if (ganzeNachricht.startsWith("set ")) {
                    String[] setTeil = ganzeNachricht.split(" ", 3);
                    if (setTeil.length >= 3) {
                        String frage = setTeil[1];
                        String antwort = setTeil[2];
                        setzeAntwort(frage, antwort);
                        writer.println("Antwort für Frage \"" + frage + "\" gesetzt.");
                    } else {
                        writer.println("Nutze \"set <Frage> <Antwort>\" um eine vordefinierte Antwort zu setzen.");
                    }
                }
            }
            synchronized (clients) {
                clients.remove(name);
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: Fehler beim Schreiben oder Lesen!");
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

    // Übungsblatt 4: Aufgabe 4a (Funktioniert gut):
    private static void versendeNachrichtAnAlle(String sender, String textnachricht) {
        synchronized (clients) {
            for (Map.Entry<String, Socket> entry : clients.entrySet()) {
                Socket clientSocket = entry.getValue();
                try {
                    PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientWriter.println("Nachricht von " + sender + " an alle: " + textnachricht);
                } catch (IOException e) {
                    System.out.println("IOException: Nachricht konnte nicht an " + entry.getKey() + " gesendet werden!");
                    e.printStackTrace();
                }
            }
        }
    }

    // Übungsblatt 4: Aufgabe 4b (Clientliste wird nicht beim Client ausgegeben, Server funktioniert danach nicht mehr, vermutlich Deadlock bei der Überprüfung der Clients):
    private static void sendeClientListe(PrintWriter writer) {
        synchronized (clients) {
            writer.println("Verbunden Clients: " + String.join(", ", clients.keySet()));
        }
    }

    private static void checkInactiveClients() {
        synchronized (clients) {
            Iterator<Map.Entry<String, Socket>> iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Socket> entry = iterator.next();
                try {
                    PrintWriter clientWriter = new PrintWriter(entry.getValue().getOutputStream(), true);
                    clientWriter.println("Ping");
                    BufferedReader clientReader = new BufferedReader(new InputStreamReader(entry.getValue().getInputStream()));
                    if (!clientReader.readLine().equals("Pong")) {
                        entry.getValue().close();
                        iterator.remove();
                    }
                } catch (IOException e) {
                    try {
                        entry.getValue().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    iterator.remove();
                }
            }
        }
    }

    // Übungsblatt 4: Aufgabe 4c (Funktioniert theoretisch mit allen Fragen, solange in der Frage kein Leerzeichen steht, Leider kommt keine Antwort auf die Frage zurück):
    private static void sendeFrage(String sender, String empfaenger, String frage, PrintWriter senderWriter) {
        Socket recipientSocket;
        synchronized (clients) {
            recipientSocket = clients.get(empfaenger);
        }
        if (recipientSocket != null) {
            try {
                PrintWriter empfaengerWriter = new PrintWriter(recipientSocket.getOutputStream(), true);
                empfaengerWriter.println("Frage von " + sender + ": " + frage);
                BufferedReader empfaengerReader = new BufferedReader(new InputStreamReader(recipientSocket.getInputStream()));
                String antwort = empfaengerReader.readLine();
                if (antwort != null && antwort.startsWith("Antwort:")) {
                    senderWriter.println(antwort);
                } else {
                    senderWriter.println(empfaenger + " konnte nicht antworten.");
                }
            } catch (IOException e) {
                senderWriter.println("IOException: Frage konnte nicht gesendet werden!");
                e.printStackTrace();
            }
        } else {
            senderWriter.println(empfaenger + " ist nicht mit dem Server verbunden.");
        }
    }

    private static void setzeAntwort(String frage, String antwort) {
        synchronized (predefinedAnswers) {
            predefinedAnswers.put(frage, antwort);
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
                    if (serverNachricht.equals("Ping")) {
                        writer.println("Pong");
                    } else if (serverNachricht.startsWith("Frage von ")) {
                        String frage = serverNachricht.split(": ", 2)[1];
                        String antwort = predefinedAnswers.get(frage);
                        if (antwort != null) {
                            writer.println("Antwort: " + antwort);
                        } else {
                            writer.println("Antwort: Keine Antwort vorhanden.");
                        }
                    } else {
                        System.out.println(serverNachricht);
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException: Fehler beim Lesen der Server Nachricht!");
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
