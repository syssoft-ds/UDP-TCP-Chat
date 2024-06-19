package de.unitrier.st.fp.s24.ueb09;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{

    private final int NUMBER_OF_WINDOWS = 3;

    @Override
    public void start(Stage stage) throws Exception {
        for (int i = 0; i < NUMBER_OF_WINDOWS; i++) {
            new ChatWindowClient().createChatWindow();
        }
    }
}
