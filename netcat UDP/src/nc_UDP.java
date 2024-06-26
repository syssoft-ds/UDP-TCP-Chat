import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class nc_UDP {

    public static void receiveLines(int port) {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("Server started, waiting for messages...");
            byte[] buffer = new byte[4096];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                String line = new String(packet.getData(), 0, packet.getLength()).trim();
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                System.out.println("Message <" + line + "> received from client " + clientAddress + ":" + clientPort);
                if (line.equalsIgnoreCase("stop")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendLines(String host, int port) {
        try (DatagramSocket clientSocket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {
            InetAddress serverAddress = InetAddress.getByName(host);
            System.out.println("Type your messages:");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                byte[] buffer = line.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
                clientSocket.send(packet);
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
            System.out.println("Usage: \"UDPProgram -l <port>\" or \"UDPProgram <ip> <port>\"");
            System.exit(1);
        }
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l")) {
            receiveLines(port);
        } else {
            sendLines(args[0], port);
        }
    }
}
