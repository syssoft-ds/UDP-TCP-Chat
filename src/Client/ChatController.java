package Client;

import Server.Server;
import javafx.application.Application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatController extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        // Start window, to open the chat clients
        stage.setTitle("Chat controller");
        BorderPane bp = new BorderPane();
        bp.setMinSize(250,80);

        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(15);
        bp.setCenter(centerBox);

        HBox userNameBox = new HBox();
        userNameBox.setSpacing(10);
        userNameBox.setAlignment(Pos.CENTER);
        Label userNameLabel = new Label("Username:");
        TextField userNameTextField = new TextField();
        userNameBox.getChildren().addAll(userNameLabel, userNameTextField);
        centerBox.getChildren().add(userNameBox);

        HBox portBox = new HBox();
        portBox.setSpacing(10);
        portBox.setAlignment(Pos.CENTER);
        Label portLabel = new Label("Enter Port:");
        TextField portTextField = new TextField();
        portBox.getChildren().addAll(portLabel, portTextField);
        centerBox.getChildren().add(portBox);

        Button startChatButton = new Button("Start Chat");
        centerBox.getChildren().add(startChatButton);

        Button startServerButton = new Button("Start ServerThread");
        centerBox.getChildren().add(startServerButton);


        stage.setScene(new Scene(bp));
        stage.show();

        startServerButton.setOnAction(event->{
            String portStringValue = portTextField.getText();

            int port = 0;
            try {
                if (portStringValue.isEmpty())throw new NumberFormatException();
                port = Integer.parseInt(portStringValue);
                if(port<1 || port>65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Port must be a number between 0 and 65535");
                return;
            }
            stage.close();
            new Server(port);;

        });

        startChatButton.setOnAction(event->{
            String userNameStringValue = userNameTextField.getText();
            if(userNameStringValue.isEmpty()) userNameStringValue="DefaultUser";

            String portStringValue = portTextField.getText();
            int port = 0;
            try {
                if (portStringValue.isEmpty())throw new NumberFormatException();
                port = Integer.parseInt(portStringValue);
                if(port<1 || port>65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Port must be a number between 0 and 65535");
                return;
            }
            new ChatWindow(userNameStringValue, port);
            stage.close();
        });


    }






}
