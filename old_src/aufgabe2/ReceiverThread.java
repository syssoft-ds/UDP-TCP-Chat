package aufgabe2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiverThread extends Thread{

    private DatagramSocket socket;
    private static final int packetSize = 4096;

    public ReceiverThread(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[packetSize];
        String line;
        do {
            DatagramPacket p = new DatagramPacket(buffer,buffer.length);
            try {
                socket.receive(p);
                line = new String(buffer,0,p.getLength(),"UTF-8");
                System.out.println(line);
                netcatUDP.setLastReceivedConnection(p.getSocketAddress());
            } catch (IOException e) {
                socket.close();
            }
        } while (!Thread.currentThread().isInterrupted());
    }
}
