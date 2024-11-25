package messages;

import util.Main;

import java.io.Serializable;

public class MessengerUser implements Serializable {

    private int userID;

    private String userName;
    private String hashedPassword;

    public MessengerUser(int userID, String userName, String hashedPassword) {
        this.userID = userID;
        this.userName = userName;
        this.hashedPassword = hashedPassword;
    }

    public Chat getChatWith(int uid) {

        for(Chat chat : Main.chats) {
            int curUID1 = chat.getUserID1();
            int curUID2 = chat.getUserID2();

            if((userID == curUID1 && uid == curUID2) ||
                    (userID == curUID2 && uid == curUID1)) {

                return chat;
            }
        }
        return null;
    }

    public Chat createChatWith(int uid) {

        Chat chat = new Chat(userID, uid);
        Main.chats.add(chat);

        return chat;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String toString() {
        return "MessengerUser{" +
                "userID=" + userID +
                ", userName='" + userName + '\'' +
                ", hashedPassword='" + hashedPassword + '\'' +
                '}';
    }
}
