

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class udp_chat {

    private static void printFatalMessage(String message) {
        System.out.println(message);
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length!= 2) {
            printFatalMessage("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        }
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l")) {
            startListeningAndCommunicating(port);
        } else {
            establishConnectionAndCommunicate(args[0], port);
        }
    }

    private static final int PACKET_SIZE = 4096;

    private static void startListeningAndCommunicating(int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(port);
        byte[] buffer = new byte[PACKET_SIZE];
        String receivedLine;
        do {
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(receivedPacket);
            receivedLine = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), "UTF-8");
            System.out.println(receivedLine);
        } while (!receivedLine.equalsIgnoreCase("stop"));
        datagramSocket.close();
    }

    private static void establishConnectionAndCommunicate(String otherHost, int otherPort) throws IOException {
        InetAddress hostAddress = InetAddress.getByName(otherHost);
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] buffer = new byte[PACKET_SIZE];
        String lineToSend;
        do {
            lineToSend = readUserInput();
            buffer = lineToSend.getBytes("UTF-8");
            DatagramPacket packetToSend = new DatagramPacket(buffer, buffer.length, hostAddress, otherPort);
            datagramSocket.send(packetToSend);
        } while (!lineToSend.equalsIgnoreCase("stop"));
        datagramSocket.close();
    }

    private static String readUserInput() {
        BufferedReader bufferedReader = null;
        boolean retry = false;
        String input = null;
        do {
            try {
                if (bufferedReader == null) {
                    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                }
                input = bufferedReader.readLine();
            } catch (Exception e) {
                System.out.printf("Exception: %s%n", e.getMessage());
                retry = true;
            }
        } while (retry);
        return input;
    }

    private BufferedReader bufferedReader = null;
}
