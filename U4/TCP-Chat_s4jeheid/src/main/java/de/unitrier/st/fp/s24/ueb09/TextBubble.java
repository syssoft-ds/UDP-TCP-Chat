package de.unitrier.st.fp.s24.ueb09;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TextBubble extends HBox {

    private final String message;
    private final int messageID;
    private final int ownID;

    public TextBubble(String message, int messageID, int ownID) {
        this.message = message;
        this.messageID = messageID;
        this.ownID = ownID;
        initTextBubble();
    }

    private void initTextBubble() {
        Label displayedText = new Label(message);
        displayedText.setPadding(new Insets(5));
        displayedText.setWrapText(true);
        getChildren().add(displayedText);

        if (messageID == ownID) {
            setAlignment(Pos.BASELINE_RIGHT);
            displayedText.setStyle("-fx-background-color: #e2fec7; -fx-background-radius: 15px; -fx-text-fill: #000000");
        } else {
            setAlignment(Pos.BASELINE_LEFT);
            displayedText.setStyle("-fx-background-color: #fefefe; -fx-background-radius: 15px; -fx-text-fill: #000000");
        }

        setMargin(this, new Insets(10,10,10,10));
        displayedText.setMaxSize(280,Integer.MAX_VALUE);
    }
}
