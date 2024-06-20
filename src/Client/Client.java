package Client;

import DataClasses.MessageData;
import DataClasses.RegisterData;
import DataClasses.UserSyncData;
import javafx.application.Platform;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private InetAddress serverAddress;
    private int serverPort;
    private final int clientPort;
    private final ChatWindow chatWindow;

    private Socket serverConnect=null;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Client(int p, ChatWindow chatWindow) {
        this.clientPort=p;
        this.chatWindow = chatWindow;
    }


    public boolean sendData(Serializable data){
        try {
            if (serverConnect==null) throw new ConnectException();
            oos.writeObject(data);
        } catch (ConnectException e){
            System.out.println("It seems like the client is not connected to a server.");
            return false;
        }catch (IOException e) {
            System.out.println("Error sending data");
            return false;
        }
        return true;
    }



    /**
     * As soon as the Client has established a connection to the server listenForMessage() method is called
     * and receives incoming MessageData objects.
     */
    private void listenForMessage() {
        new Thread(() -> {
            int errorCount=0;
            while (chatWindow.getStage().isShowing()){
                try {
                    Object obj = this.ois.readObject();

                    if (obj instanceof MessageData) {
                        MessageData messageData = (MessageData) obj;
                        System.out.println("received: " + messageData.getMessageText());
                        Platform.runLater(() -> this.chatWindow.addMessageToScreen(messageData));
                    }

                    if (obj instanceof UserSyncData) this.chatWindow.addUsersToList((UserSyncData)obj);

                } catch (ClassNotFoundException e) {
                    System.out.println("Error receiving message");
                } catch (IOException e) {
                    System.out.println("It seems like server closed connection");
                    if( errorCount++>15) break;
                }
            }
        }).start();
    }


    public void connectToServer(String userName, String[] connectData) {

        int serverPort = 0;
        String serverAddress = "";
        try {
            if (connectData.length!=2)throw new IllegalArgumentException();
            if (connectData[0].isEmpty()) connectData[0]="localhost";
            if (connectData[1].isEmpty())throw new NumberFormatException();
            serverPort = Integer.parseInt(connectData[1]);
            if(serverPort<1 || serverPort>65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Port must be a number between 0 and 65535");
            return;
        }

        InetAddress ip;
        try {
            ip = InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            System.out.println("No valid IP-Address.");
            return;
        }

        try {
            this.serverConnect = new Socket(ip,serverPort);
            this.oos = new ObjectOutputStream(this.serverConnect.getOutputStream());
            this.ois = new ObjectInputStream(this.serverConnect.getInputStream());
        } catch (IOException e) {
            System.out.println("Error during connection. ");
            return;
        }
        this.serverAddress = ip;
        this.serverPort = serverPort;
        sendData(new RegisterData(userName));
        listenForMessage();

    }

}
