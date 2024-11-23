package messages;

import serializable.Message;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Chat implements Serializable {

    private final List<Message> messages = new CopyOnWriteArrayList<>();

    private final int userID1;
    private final int userID2;

    private int lastMessageID = 0;

    public Chat(int userID1, int userID2) {
        this.userID1 = userID1;
        this.userID2 = userID2;
    }

    public int getNextMessageID() {
        return ++lastMessageID;
    }

    public int getUserID1() {
        return userID1;
    }

    public int getUserID2() {
        return userID2;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean deleteMessage(int messageID) {
        for(Message message : messages) {
            if(message.getId() == messageID) {
                messages.remove(message);
                return true;
            }
        }
        return false;
    }
}
