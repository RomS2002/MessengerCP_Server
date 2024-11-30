package connection;

import messages.Chat;
import messages.MessengerUser;
import serializable.Message;
import serializable.User;
import util.Main;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthorizedConnection extends Connection {

    private MessengerUser user;
    private Scanner scanner;
    private boolean isRunning = true;

    public AuthorizedConnection(Socket socket, ObjectInputStream in, ObjectOutputStream out,
                                MessengerUser user) throws IOException {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.user = user;

        scanner = new Scanner(in);
    }

    public MessengerUser getUser() {
        return user;
    }

    private void getUsers() {

        List<User> result = new CopyOnWriteArrayList<>();

        for(MessengerUser curMessengerUser : Main.users) {
            User curUser = new User();
            curUser.setUserName(curMessengerUser.getUserName());
            curUser.setUserID(curMessengerUser.getUserID());
            result.add(curUser);
        }
        try {
            out.writeObject(result);
            out.flush();

        } catch(IOException e) {
            System.err.println("ERROR: Ошибка отправки результата getUsers");
        }
    }

    private void getMessagesInChat(int userID) {

        Chat chat = user.getChatWith(userID);
        if(chat == null) {
            chat = user.createChatWith(userID);
        }

        List<Message> messages = new ArrayList<>(chat.getMessages());

        try {
            out.writeObject(messages);
            out.flush();
        } catch(IOException e) {
            System.err.println("ERROR: Ошибка отправки результата getMessagesInChat");
        }
    }

    private void sendMessage(Message message) {
        String toName = message.getReceiverName();
        MessengerUser toUser = null;
        for(MessengerUser curUser : Main.users) {
            if(curUser.getUserName().equals(toName)) {
                toUser = curUser;
                break;
            }
        }
        if(toUser == null) {
            try {
                out.write("Incorrect message\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }

            System.err.println("sendMessage: Некорректное сообщение");
            return;
        }

        Chat chat = user.getChatWith(toUser.getUserID());
        if(chat == null) {
            chat = user.createChatWith(toUser.getUserID());
        }
        int id = chat.getNextMessageID();
        message.setId(id);
        chat.addMessage(message);

        try {
            out.write("OK\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch(IOException e) {
            System.err.println("ERROR: неопределённая ошибка!");
            close();
        }
    }

    private void deleteMessage(int toUID, int messageID) {

        Chat chat = user.getChatWith(toUID);
        if(chat == null) {
            try {
                out.write("Chat do not exists\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch(IOException e) {
                System.err.println("ERROR: неопределённая ошибка!");
                close();
            }
            return;
        }

        boolean result = chat.deleteMessage(messageID);
        if(result) {
            try {
                out.write("OK\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch(IOException e) {
                System.err.println("ERROR: неопределённая ошибка!");
                close();
            }
        } else {
            try {
                out.write("Incorrect message ID\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch(IOException e) {
                System.err.println("ERROR: неопределённая ошибка!");
                close();
            }
        }
    }

    @Override
    public void run() {
        try {
            while(isRunning) {
                String command = scanner.nextLine();

                switch(command) {
                    case "getUsers":
                        getUsers();
                        break;
                    case "getMessagesInChat":
                        int userID = 0;
                        try {
                            userID = (Integer) in.readObject();
                        } catch(IOException | ClassNotFoundException e) {
                            System.err.println("getMessagesInChat: ошибка получения UID");
                        }
                        getMessagesInChat(userID);
                        break;
                    case "sendMessage":
                        Message message;
                        try {
                            message = (Message) in.readObject();
                        } catch(Exception e) {
                            System.err.println("sendMessage: ошибка получения сообщения");
                            continue;
                        }
                        sendMessage(message);
                        break;
                    case "deleteMessage":
                        int toUID;
                        int messageID;

                        try {
                            toUID = (Integer) in.readObject();
                            messageID = (Integer) in.readObject();

                        } catch(Exception e) {
                            System.err.println("deleteMessage: ошибка получения сообщения");
                            continue;
                        }
                        deleteMessage(toUID, messageID);
                        break;
                    case "disconnect":
                        isRunning = false;
                        close();
                        break;
                }
            }
        }
        catch(RuntimeException e) {
            System.out.println("Соединение с клиентом было разорвано!");
            close();
        }
    }
}
