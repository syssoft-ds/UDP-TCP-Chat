package de.unitrier.st.fp.s24.ueb09;

import de.unitrier.st.fp.s24.ueb09.common.Handshake;
import de.unitrier.st.fp.s24.ueb09.common.Message;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatWindowClient extends Thread {

    private final VBox messageContainer = new VBox(5);
    private int chatWindowID;
    //added these:
    private Socket clientToServer;

    private ObjectInputStream clientInputStream;
    private ObjectOutputStream clientOutputStream;

    //-----
    public void createChatWindow() {
        BorderPane root = new BorderPane();

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        menuBar.getMenus().add(menu);

        menu.getItems().add(new MenuItem("Backup"));
        menu.getItems().add(new MenuItem("Restore"));
        root.setTop(menuBar);
        ScrollPane messageScroller = new ScrollPane(messageContainer);
        messageScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messageScroller.setPrefHeight(300);
        messageScroller.setFitToWidth(true);
        messageContainer.setPrefHeight(373);
        messageContainer.setStyle("-fx-background-color: #f0e7de;");

        HBox hBox = new HBox();
        TextField msgField = new TextField();
        Button sendButton = new Button("Send");
        sendButton.setMinWidth(50);

        sendButton.setOnAction(actionEvent -> {
            if (!msgField.getText().equals(""))
                sendMessage(msgField.getText());
            msgField.clear();
        });

        msgField.setPrefWidth(250);
        hBox.getChildren().add(msgField);
        hBox.getChildren().add(sendButton);
        root.setCenter(messageScroller);
        root.setBottom(hBox);

        Scene scene = new Scene(root, 300, 400);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (!msgField.getText().equals("")) {
                    sendMessage(msgField.getText());
                }
                msgField.clear();
            }
        });
        //program fails when no Server available to connect to,
        //How to make sure that a Server will be available???
        connect();
        Stage stage = new Stage();
        stage.setTitle("User " + chatWindowID);
        stage.setScene(scene);
        stage.show();
    }

    public void displayReceiveMessage(Message msg) {
        String message;
        if (msg.getId() != this.chatWindowID) {
            message = "User " + msg.getId() + ": " + msg.getText();
        } else {
            message = msg.getText();
        }
        TextBubble msgBubble = new TextBubble(message, msg.getId(), this.chatWindowID);
        this.messageContainer.getChildren().add(msgBubble);
    }

    //TODO: Is @Override needed or what exactly does it do as a rule of thumb for my brain ?
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && this.clientToServer.isConnected()) {
                Object object = this.clientInputStream.readObject();
                Class<?> clazz = object.getClass(); //get class of object to access public methods

                try {
                    //!don't use clazz.getField() for private fields as it only returns public fields
                    Field privateField = clazz.getDeclaredField("id");
                    privateField.setAccessible(true);
                    System.out.println("\"Security\": " + privateField.get(object));
                    /*
                        !remember to use object to get object (the current instance) its own value
                        object = current suspect
                        clazz = I want a fruit ==> All instances of class fruit
                        privateField = with encoded DNA squence in its genome ==> store path to encoded(private) DNA Squence
                        privateField.get(object) = get the encoded DNA squence of the current suspect(object)

                    */
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                Method getIdMethod = clazz.getMethod("getId");
                Method getTextMethod = clazz.getMethod("getText");
                int id = (int) getIdMethod.invoke(object);
                String text = (String) getTextMethod.invoke(object);
                Message message = new Message(text, id);
                /*
                     //without reflection, using cast to get Message Object
                     Message message = (Message) this.clientInputStream.readObject();
                */
                Platform.runLater(() -> displayReceiveMessage(message));

            }
        } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if (this.clientToServer != null && this.clientToServer.isConnected())
                try {
                    this.clientToServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void sendMessage(String text) {
        try {
            Message message = new Message(text, this.chatWindowID);
            clientOutputStream.writeObject(message);
            clientOutputStream.flush();//* Flush to clear output stream for next message
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Aufgabe 1:
    public void connect() {
        //TODO Connect to Server

        //!try-with-resources doesn't allow attribute to be assigned to new value
        try {
            clientToServer = new Socket("localhost", 3321);
            System.out.println("Connected to Server");
            //create streams for communication with Server (ObjectStreams)
            this.clientInputStream = new ObjectInputStream(this.clientToServer.getInputStream());
            this.clientOutputStream = new ObjectOutputStream(this.clientToServer.getOutputStream());

            //using reflection to replace the cast to Handshake
            //Handshake clientHandshake = (Handshake) this.clientInputStream.readObject();
            //throws ClassNotFoundException on readObject
            Object object = this.clientInputStream.readObject();
            Class<?> clazz = object.getClass();
            Method getIdMethod = clazz.getDeclaredMethod("getId");
            this.chatWindowID = (int) getIdMethod.invoke(object);//cast object to int

            Object arrayObject = clazz.getDeclaredMethod("getMessagesList").invoke(object);
            //!get the ArrayList object to use invoke later on to get ArrayList Methods instead of Handshake
            Class<?> arrayListClazz = arrayObject.getClass();
            Method toArrayMethod = arrayListClazz.getDeclaredMethod("toArray");
            Object[] messageArray = (Object[]) toArrayMethod.invoke(arrayObject);//
            for (Object message : messageArray) {
                Method getIdMethodMessage = message.getClass().getDeclaredMethod("getId");
                Method getTextMethodMessage = message.getClass().getDeclaredMethod("getText");
                int id = (int) getIdMethodMessage.invoke(message);
                String text = (String) getTextMethodMessage.invoke(message);
                displayReceiveMessage(new Message(text, id));
            }

            //Aufgabe 3: catch up with messages sent before connecting
//            for (Message message : clientHandshake.getMessagesList()){
//                displayReceiveMessage(message);
//            }

            // Start receiving Thread when connected
            start();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
