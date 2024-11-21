package util;


import messages.Chat;
import messages.MessengerUser;

import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    public static final String address = "localhost";
    public static final int port = 23456;

    public static CopyOnWriteArrayList<Chat> chats = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<MessengerUser> users = new CopyOnWriteArrayList<>();

    public static int getNextUserID() {
        if(users.isEmpty()) {
            return 1;
        }
        return users.getLast().getUserID() + 1;
    }

    public static void main(String[] args) {
        ServerConsole serverConsole = new ServerConsole();
        serverConsole.start();

        ConnectionListener connectionListener = new ConnectionListener();
        connectionListener.start();

        try {
            serverConsole.join();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        connectionListener.close();
    }
}
