package aufgabe2;

import utils.Contact;
import utils.UserAlreadyExistsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

public class CommandThread extends Thread{

    private final DatagramSocket socket;

    public CommandThread(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            String[] splitData = readString().split(" ",2);
            String command = splitData[0].toLowerCase();
            switch (command){
                case "stop":

                    Thread.currentThread().interrupt();
                    System.exit(0);
                    break;

                case "add":
                    if(splitData.length!=2){
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("add <name> <ip> <port>");
                        break;
                    }
                    try {
                        Contact.addContact(splitData[1]);
                    } catch (UnknownHostException | NumberFormatException | UserAlreadyExistsException e) {
                        System.out.println(e.getMessage());
                    } catch (IllegalArgumentException e){
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("add <name> <ip> <port>");
                    }
                    break;

                case "addlast":
                    if(splitData.length!=2 || splitData[1].split(" ").length<1) {
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("addlast <name>");
                        break;
                    }

                    SocketAddress sa = netcatUDP.getLastReceivedConnection();
                    if(sa==null){
                        System.out.println("No last received contact information.");
                        break;
                    }
                    String contactData = splitData[1] + " " + sa.toString().split(":")[0].replace("/","") + " "
                            + sa.toString().split(":")[1];

                    try {
                        Contact.addContact(contactData);
                    } catch (UnknownHostException | NumberFormatException | UserAlreadyExistsException e) {
                        System.out.println(e.getMessage());
                    } catch (IllegalArgumentException e){
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("addlast <name>");
                    }
                    break;

                case "show":
                    Contact.getContacts().forEach(System.out::println);
                    break;

                case "send":
                    if(splitData.length!=2){
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("send <receiver> <message>");
                        break;
                    }
                    try {
                        sendMessage(splitData[1]);
                    } catch (IOException e) {
                        System.out.println("An error occurred while sending.");
                    } catch (ArrayIndexOutOfBoundsException e){
                        System.out.println("The message was not well formatted. Needed format:");
                        System.out.println("send <name> <message>");
                    } catch (NoSuchElementException e){
                        System.out.println(e.getMessage());
                    }
                    break;

                case "help":
                    System.out.println("add : Adding a new contact.\n" +
                            "addlast : Adding the author from the last received message.\n" +
                            "send : Sending a message to a contact\n" +
                            "show : List all saved contacts.\n" +
                            "stop : End the Program.\n" +
                            "help : Well help.");
                    break;

                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Enter help to get full list of commands.");
            }
        }
    }

    private void sendMessage (String msgData ) throws IOException,
            ArrayIndexOutOfBoundsException, NoSuchElementException {
        String[] splitData = msgData.split(" ",2);

        String receiverName = splitData[0];
        String msg = splitData[1];
        Contact contact = Contact.getContactByName(receiverName);

        byte[] buffer = msg.getBytes("UTF-8");
        DatagramPacket p = new DatagramPacket(buffer,buffer.length,contact.getIp(),contact.getPort());
        this.socket.send(p);
    }

    private static String readString () {
        BufferedReader br = null;
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

}
