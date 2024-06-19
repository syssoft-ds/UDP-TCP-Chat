package de.unitrier.st.fp.s24.ueb09.server;

import de.unitrier.st.fp.s24.ueb09.common.Handshake;
import de.unitrier.st.fp.s24.ueb09.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ConnectedClient extends Thread {
    //added this:
    private ObjectOutputStream clientOutputStream;
    private ObjectInputStream clientInputStream;
    //-----

    public ConnectedClient(Socket socket) {
        //TODO create streams
        try {
            this.clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.clientInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //TODO Sending Handshake and waiting for incoming messages
        try {
            this.clientOutputStream.writeObject(new Handshake(Server.getInstance().getClientID(), Server.getInstance().getMessageList()));
            this.clientOutputStream.flush();
            //! flush is used to clear the value
            while (!Thread.currentThread().isInterrupted()) {
//                Message message = (Message) this.clientInputStream.readObject();
                //Aufgabe 4:  Reflection
                Object object4Message = this.clientInputStream.readObject();
                Class<?> clazz = object4Message.getClass();
                Method getTextMethod = clazz.getDeclaredMethod("getText");
                String messageText= (String) getTextMethod.invoke(object4Message);
                System.out.println("Received: " + messageText);
                Method getIdMethod = clazz.getDeclaredMethod("getId");
                int messageId = (int)getIdMethod.invoke(object4Message);
                Message message = new Message(messageText,messageId);
                //--- End of replacing Cast to Message with reflections ----
                    Server.getInstance().addMessageToList(message);//sync still needed
                for (ConnectedClient connectedClient : Server.getInstance().getClientsList()) {
                    //Multiple Threads access these resources from Server -> synchronize
                    connectedClient.sendMessageToClient(message);
                }

            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException iOException) {

            try {
                this.clientInputStream.close();
                this.clientOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                this.clientInputStream.close();
                this.clientOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendMessageToClient(Object object) {
        //* send Message to client
        try {
            this.clientOutputStream.writeObject(object);
            this.clientOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
