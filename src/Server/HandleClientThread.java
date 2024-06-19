package Server;

import DataClasses.MessageData;
import DataClasses.RegisterData;
import DataClasses.UserSyncData;
import Utils.UserAlreadyExistsException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class HandleClientThread extends Thread{
    private static final ArrayList<HandleClientThread> userList = new ArrayList<>();

    private Contact contact;
    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public HandleClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.ois = new ObjectInputStream(clientSocket.getInputStream());
            this.oos = new ObjectOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error building connection.");
        }
        userList.add(this);
    }

    @Override
    public void run() {
        int errorCount=0;
        try{
            MessageData messageData = null;
            while (!currentThread().isInterrupted()){
                try {
                    Object obj = ois.readObject();
                    if (obj instanceof MessageData) messageHander((MessageData) obj);
                    if (obj instanceof RegisterData) registerClient((RegisterData) obj);
                } catch (ClassNotFoundException e) {
                    System.out.println("Error receiving message");
                } catch (IOException e) {
                    System.out.println("It seems like " + contact.getUserName() + " closed connection");
                    if( errorCount++>15) {
                        userList.remove(this);
                        this.oos.close();
                        synchUsers();
                        break;
                    }
                } catch (UserAlreadyExistsException e){
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while receiving messages. " + e.getMessage());
            Arrays.stream(e.getStackTrace()).forEach(System.out::println);

        }
    }

    /**
     * The server calls this method to decide if it's a command that has to be executed or if it
     * is a message to forward to its destination.
     * @param messageData
     */
    private void messageHander (MessageData messageData)throws IOException{

        if(messageData.sendToAll()) {
            for(HandleClientThread c : userList){
                if (!messageData.getSender().equals(c.getContact().getUserName())) {
                    c.getOutStream().writeObject(messageData);
                }
            }
            return;
        }


        try {
            ObjectOutputStream receiverStream = userList.stream()
                    .filter(c -> c.getContact().getUserName().equals(messageData.getReceiver()))
                    .findFirst().orElseThrow().getOutStream();
            receiverStream.writeObject(messageData);
            System.out.println("Message forwarded.");
        } catch (IOException e) {
            System.out.println("Error during message forwarding.");
        } catch (NoSuchElementException e){
            System.out.println("No such username in register");
        }


    }

    /**
     * The server calls this method to add a new contact to it's register or
     * to update the register entry with the new connection information.
     * @param registerData
     */
    private void registerClient(RegisterData registerData) throws UserAlreadyExistsException, IOException{
        InetAddress clientIP = clientSocket.getInetAddress();
        int port = clientSocket.getPort();
        if (!Contact.userExists(registerData.getUserName(),clientIP, port)){
            this.contact = Contact.addContact(registerData.getUserName(), clientIP, port);
        }else{
            HandleClientThread tmpClientThread = userList.stream()
                    .filter(c -> c.getContact().getUserName().equals(registerData.getUserName()))
                    .findFirst().orElseThrow();
            tmpClientThread.getContact().updateContact(this.clientSocket.getInetAddress(), this.clientSocket.getPort());
        }
        synchUsers();

    }

    private void synchUsers() throws IOException{
        UserSyncData userSyncData = new UserSyncData(userList.stream().map(u -> u.getContact().getUserName()).toList());

        for(HandleClientThread c : userList){
            c.getOutStream().writeObject(userSyncData);
        }
    }

    public Contact getContact() {return this.contact;}
    public ObjectOutputStream getOutStream(){return this.oos;}


}
