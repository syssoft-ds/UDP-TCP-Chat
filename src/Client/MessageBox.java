package Client;

import DataClasses.MessageData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/*
    This class creates the message node
 */
public class MessageBox extends HBox {
    private MessageData messageData;
    private boolean sender;

    MessageBox(MessageData msgData, boolean sender){
        this.sender=sender;
        this.messageData=msgData;
        buildMessage();
    }

    private void buildMessage(){
        this.setPrefWidth(ChatWindow.WINDOW_WIDTH);
        this.setPadding(new Insets(2,0,2,0));


        // Creating the messageLabel
        Label messageLabel = new Label( messageData.getSender() + ": " +messageData.getMessageText());
        messageLabel.setMaxWidth(ChatWindow.WINDOW_WIDTH-100);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(5,5,5,5));


        // Change the MessageMox background color and place it according to its origin.
        if (this.sender) {
            messageLabel.setStyle("-fx-background-color: #ceff8f;-fx-background-radius: 10px");
            this.setAlignment(Pos.BASELINE_RIGHT);
        } else {
            messageLabel.setStyle("-fx-background-color: #e3e3e3;-fx-background-radius: 10px");
            this.setAlignment(Pos.BASELINE_LEFT);
        }

        this.getChildren().add(messageLabel);
    }

}