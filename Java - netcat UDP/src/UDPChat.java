import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

// Für mein eigenes Verständnis habe ich, neben der Nutzung eines KI-Assistenten, einigen Variablen und Methoden andere Namen gegeben.
// Fairerweise muss ich zugeben, dass selbiger KI-Assistent mich auch beim Schreiben der neuen Funktionen an der ein oder anderen Stelle unterstützt hat.

public class UDPChat {

    private static void fatal(String comment) {
        System.out.println(comment);
        System.exit(-1);
    }

    private static final int packetSize = 4096;
    private static Map<String, SocketAddresse> userRegistry = new HashMap<>();

    // ************************************************************************
    // MAIN /
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 3)
            fatal("Usage: \"<name> -l <port>\" or \"<name> <ip> <port>\"");
        String name = args[0];
        int port = Integer.parseInt(args[2]);
        if (args[1].equalsIgnoreCase("-l"))
            listenAndTalk(name, port);
        else
            connectAndTalk(name, args[1], port);
    }

    // ************************************************************************
    // listenAndTalk
    // ************************************************************************
    private static void listenAndTalk(String eigenerName, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buffer = new byte[packetSize];
        String ganzeNachricht;
        System.out.println(eigenerName + " hat den Chat eröffnet!");

        new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String empfangeneNachricht = new String(buffer, 0, packet.getLength(), "UTF-8");
                    verarbeiteNachricht(empfangeneNachricht, packet.getAddress(), packet.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (!(ganzeNachricht = readString()).equalsIgnoreCase("stop")) { // Tut euch den gefallen und stoppt NICHT
            if (ganzeNachricht.startsWith("register ")) { //Empfänger-Registrierung    // so, stoppt mit Strg+c, sonst fliegen
                String[] nachrichtenteil = ganzeNachricht.split(" ");            // Unmengen an Exceptions auf die Konsole.
                if (nachrichtenteil.length == 4) {
                    String empfaengerName = nachrichtenteil[1];
                    String empfaengerIp = nachrichtenteil[2];
                    int empfaengerPort = Integer.parseInt(nachrichtenteil[3]);
                    fuegeFreundHinzu(eigenerName, empfaengerName, empfaengerIp, empfaengerPort, socket);
                }
            } else if (ganzeNachricht.startsWith("send ")) {
                String[] parts = ganzeNachricht.split(" ", 3);
                if (parts.length == 3) {
                    String empfaengerName = parts[1];
                    String textnachricht = parts[2];
                    verschickeTextnachricht(eigenerName, empfaengerName, textnachricht, socket);
                }
            }
        }

        socket.close();
    }

    // ************************************************************************
    // connectAndTalk
    // ************************************************************************
    private static void connectAndTalk(String name, String empfaengerIP, int empfaengerPort) throws IOException {
        InetAddress empfängerIP = InetAddress.getByName(empfaengerIP);
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = new byte[packetSize];
        String ganzeNachricht;

        // Füge Freunde hinzu:
        String registerMessage = "register " + name + " " + InetAddress.getLocalHost().getHostAddress() + " " + socket.getLocalPort();
        buffer = registerMessage.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, empfängerIP, empfaengerPort);
        socket.send(packet);

        new Thread(() -> {
            byte[] empfangeneNachricht = new byte[packetSize];
            while (true) {
                try {
                    DatagramPacket empfangenesPacket = new DatagramPacket(empfangeneNachricht, empfangeneNachricht.length);
                    socket.receive(empfangenesPacket);
                    String empfangen = new String(empfangeneNachricht, 0, empfangenesPacket.getLength(), "UTF-8");
                    verarbeiteNachricht(empfangen, empfangenesPacket.getAddress(), empfangenesPacket.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Normale Nachrichten:
        while (!(ganzeNachricht = readString()).equalsIgnoreCase("stop")) {
            if (ganzeNachricht.startsWith("send ")) {
                String[] nachrichtenteil = ganzeNachricht.split(" ", 3);
                if (nachrichtenteil.length == 3) {
                    String empfaengername = nachrichtenteil[1];
                    String textnachricht = nachrichtenteil[2];
                    verschickeTextnachricht(name, empfaengername, textnachricht, socket);
                }
            }
        }

        socket.close();
    }

    private static void verarbeiteNachricht(String ganzeNachricht, InetAddress IPadresse, int port) {
        String[] teilnachricht = ganzeNachricht.split(" ", 4);
        if (teilnachricht[0].equalsIgnoreCase("register")) {
            String senderName = teilnachricht[1];
            String senderIp = teilnachricht[2];
            int senderPort = Integer.parseInt(teilnachricht[3]);
            SocketAddresse freundAdresse = new SocketAddresse(senderIp, senderPort);
            userRegistry.put(senderName, freundAdresse);
            System.out.println(senderName + " als Freund hinzugefügt (\"register " + senderName + " " + senderIp + " " + senderPort + "\" um bei " + senderName + " neu hinzugefügt zu werden!)");
        } else if (teilnachricht[0].equalsIgnoreCase("nachricht")) {
            String sender = teilnachricht[1];
            String textnachricht = teilnachricht[3];
            System.out.println("Nachricht von " + sender + ": " + textnachricht);
        }
    }

    private static void fuegeFreundHinzu(String name, String freundName, String freundIP, int freundPort, DatagramSocket socket) throws IOException {
        InetAddress freundIPAdresse = InetAddress.getByName(freundIP);
        String registerMessage = "register " + name + " " + InetAddress.getLocalHost().getHostAddress() + " " + socket.getLocalPort();
        byte[] buffer = registerMessage.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, freundIPAdresse, freundPort);
        socket.send(packet);
        userRegistry.put(freundName, new SocketAddresse(freundIP, freundPort));
    }

    private static void verschickeTextnachricht(String eigenerName, String empfaenger, String textnachricht, DatagramSocket socket) throws IOException {
        SocketAddresse empfaengerIP = userRegistry.get(empfaenger);
        if (empfaengerIP != null) {
            String ganzeNachricht = "nachricht " + eigenerName + " " + empfaenger + " " + textnachricht;
            byte[] buffer = ganzeNachricht.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, empfaengerIP.getAddress(), empfaengerIP.getPort());
            socket.send(packet);
        } else {
            System.out.println("Freund nicht gefunden :(");
        }
    }

    private static String readString() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        try {
            input = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static class SocketAddresse {
        private final String ip;
        private final int port;

        public SocketAddresse(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public InetAddress getAddress() throws IOException {
            return InetAddress.getByName(ip);
        }

        public int getPort() {
            return port;
        }
    }
}