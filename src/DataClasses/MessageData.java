package DataClasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MessageData implements Serializable {
    private String messageText;
    private String sender;
    private String receiver;
    private LocalDateTime dateTime;
    private boolean sendToAll;



    public MessageData(String messageText, String sender, String receiver, boolean sendToAll) {
        this.messageText = messageText;
        this.sender = sender;
        this.receiver = receiver;
        this.dateTime = LocalDateTime.now();
        this.sendToAll = sendToAll;
    }

    public String getMessageText() {
        return this.messageText;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public boolean sendToAll(){
        return this.sendToAll;
    }

}
