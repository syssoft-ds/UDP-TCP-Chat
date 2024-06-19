package Client;

import DataClasses.MessageData;
import DataClasses.UserSyncData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ChatWindow {
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 500;
    private final BorderPane root;
    private VBox messageContainer;
    private ComboBox<String> contactComboBox;
    private Stage stage;

    private final Client client;
    private String username;

    public ChatWindow(String userName, int port) {
        this.username = userName;
        this.root = new BorderPane();
        this.client = new Client(port, this);
        createChatWindow();
    }

    private void createChatWindow(){
        this.stage = new Stage();
        stage.setTitle("Chat");

        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.setMaxWidth(WINDOW_WIDTH);

        // Create Menu
        Menu fileMenu = new Menu("File");
        MenuItem connectMenuButton = new MenuItem("Connect");
        fileMenu.getItems().add(connectMenuButton);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);

        root.setTop(menuBar);

        connectMenuButton.setOnAction(event -> getServerConnectionPrompt());

        // Messagefield
        // ScrollPane is the container for the parent(messageContainer) that hold the MessageBox nodes.
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        this.messageContainer = new VBox();
        this.messageContainer.setStyle("-fx-background-color: #f2e3b8");
        this.messageContainer.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.messageContainer.setPadding(new Insets(0, 10, 0, 10));

        scrollPane.setContent(this.messageContainer);
        this.root.setCenter(scrollPane);

        // A VBox to wrap the bottomBoxes together
        VBox bottomBox = new VBox();
        root.setBottom(bottomBox);

        // A HBox to wrap the bottom items together
        HBox uppeBottomBox = new HBox();
        //uppeBottomBox.setPrefHeight(50);
        bottomBox.getChildren().add(uppeBottomBox);
        // ComboBox for user selection
        this.contactComboBox = new ComboBox<>();
        uppeBottomBox.getChildren().add(this.contactComboBox);
        // Textfield
        TextField messageField = new TextField();
        HBox.setHgrow(messageField, Priority.ALWAYS);
        uppeBottomBox.getChildren().add(messageField);

        // A HBox to wrap the bottom items together
        HBox lowerBottomBox = new HBox();
        //lowerBottomBox.setPrefHeight(50);
        lowerBottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().add(lowerBottomBox);
        // Send to all chackbox
        Label sendToAllLabel = new Label("Check to send to everyone");
        sendToAllLabel.setPadding(new Insets(0,20,0,0));
        CheckBox sendToAllBox = new CheckBox();
        lowerBottomBox.getChildren().addAll(sendToAllLabel,sendToAllBox);


        // Send Button
        Button sendButton = new Button("Send");
        sendButton.setMinWidth(75.0);
        uppeBottomBox.getChildren().add(sendButton);
        sendButton.setOnAction(event -> {
            MessageData messageData = new MessageData(
                    messageField.getText(),
                    this.username, contactComboBox.getValue(),
                    sendToAllBox.isSelected());

            if (!this.client.sendData(messageData)) return;
            this.addMessageToScreen(messageData);
            messageField.setText("");
        });

        // Send message when enter key is pressed
        messageField.setOnKeyPressed(event ->{
            if (event.getCode()==KeyCode.ENTER) sendButton.fire();
        });

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void addUsersToList(UserSyncData userSyncData){
        contactComboBox.getItems().clear();
        for(String user : userSyncData.getUserNameList()){
            if (!user.equals(this.username)){
                contactComboBox.getItems().add(user);
            }
        }

    }

    public void addMessageToScreen(MessageData messageData){
        MessageBox messageBox = new MessageBox(messageData, (messageData.getSender().equals(this.username)));
        this.messageContainer.getChildren().add(messageBox);
    }

    public void getServerConnectionPrompt(){
        Stage stage = new Stage();
        stage.setTitle("Please enter Server ip and port");

        VBox promptRoot = new VBox();
        promptRoot.setMinSize(250,80);
        promptRoot.setAlignment(Pos.CENTER);
        promptRoot.setSpacing(15);

        HBox ipBox = new HBox();
        ipBox.setSpacing(10);
        ipBox.setAlignment(Pos.CENTER);
        Label ipLabel = new Label("Enter IP:");
        TextField ipTextField = new TextField();
        ipBox.getChildren().addAll(ipLabel, ipTextField);
        promptRoot.getChildren().add(ipBox);

        HBox portBox = new HBox();
        portBox.setSpacing(10);
        portBox.setAlignment(Pos.CENTER);
        Label portLabel = new Label("Enter Port:");
        TextField portTextField = new TextField();
        portBox.getChildren().addAll(portLabel, portTextField);
        promptRoot.getChildren().add(portBox);

        Button okButton = new Button("Ok");
        promptRoot.getChildren().add(okButton);


        stage.setScene(new Scene(promptRoot));
        stage.show();

        okButton.setOnAction(event->{
            client.connectToServer(this.username, new String[]{ipTextField.getText(), portTextField.getText()});
            stage.close();
        });

    }

    public Stage getStage() {
        return stage;
    }
}