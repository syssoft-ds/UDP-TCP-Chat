package aufgabe3;

import utils.Contact;
import utils.UserAlreadyExistsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerThread extends Thread{
    private static int port;
    private static ServerSocket server;
    private static Map<Contact,Socket> clients = new HashMap<>();

    public ServerThread(int p) {
        port=p;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("An error occurred while starting the server thread.");
        }
        System.out.println("This server can be reached under: " + "\tPort: " + server.getLocalPort());
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket client = server.accept();
                new Thread(() -> {
                    try(BufferedReader r = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter w = new PrintWriter(client.getOutputStream(),true)) {
                        String line;
                        do {
                            line = r.readLine();
                            String[] splitData = line.split(" ",3);
                            if (splitData.length == 2 && splitData[0].equals("register")){
                                try {
                                    Contact tmpContact = Contact.addContact(splitData[1], client.getInetAddress(), client.getPort());
                                    clients.put(tmpContact, client);
                                } catch (UserAlreadyExistsException e) {
                                    System.out.println(e.getMessage());
                                    w.println(e.getMessage());
                                }
                            }

                            if(splitData.length == 1 && splitData[0].equals("show")){
                                Contact.getContacts().forEach(c -> w.println(c.toString()));
                            }

                            if (splitData.length == 3 && splitData[0].equals("send")){
                                System.out.println("test");
                                try {
                                    Socket receiverSocket= clients.get(Contact.getContactByName(splitData[1]));
                                    PrintWriter pr = new PrintWriter(receiverSocket.getOutputStream(),true);
                                    pr.println(splitData[2]);
                                } catch (NoSuchElementException e) {
                                    System.out.println("Error" + e.getMessage());
                                    w.println(e.getMessage());
                                } catch (IOException e){
                                    System.out.println("SERVER: Error forwarding message.");
                                    w.println("SERVER: Error forwarding message.");
                                }

                            }

                            System.out.println(line);
                        } while (!line.equalsIgnoreCase("stop"));
                        client.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred while receiving message. " + e.getMessage());
                        Arrays.stream(e.getStackTrace()).forEach(System.out::println);

                    }
                }).start();
            } catch (IOException e) {
                System.out.println("An error occurred during connection.");
            }
        }

    }
}
