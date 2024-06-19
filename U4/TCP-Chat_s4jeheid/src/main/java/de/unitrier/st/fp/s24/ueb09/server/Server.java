package de.unitrier.st.fp.s24.ueb09.server;

import de.unitrier.st.fp.s24.ueb09.common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server extends Thread {

    private static Server instance;
    //added these:
    private final int port= 3321;
    //A2
    private final ArrayList<ConnectedClient> clientsList;
    private final ArrayList<Message> messageList;
    private int clientID;
    //-----
    //Server as Singleton
    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }

    public Server() {
        //because final:
        this.clientsList = new ArrayList<>();
        this.messageList = new ArrayList<>();
    }

    @Override
    public void run() {
        //TODO waiting for new clients and start new ConnectedClient Thread
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                ConnectedClient connectedClient=new ConnectedClient(serverSocket.accept());
                this.clientsList.add(connectedClient);
                connectedClient.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//Check what changes with and without synchronized?
    public synchronized ArrayList<ConnectedClient> getClientsList() {
        return clientsList;
    }

    public synchronized ArrayList<Message> getMessageList() {
        return messageList;
    }

    public synchronized void addMessageToList(Message message) {
        this.messageList.add(message);
    }

    public synchronized int getClientID() {
        return clientID++;
        //must be UNIQUE -> Server remains online,
        // but clients come and go therefore only the Server can reliably keep count.
    }
}
