package util;


import messages.Chat;
import messages.MessengerUser;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    public static final String address = "localhost";
    public static final int port = 23456;

    public static List<Chat> chats = new CopyOnWriteArrayList<>();
    public static List<MessengerUser> users = new CopyOnWriteArrayList<>();

    public static int getNextUserID() {
        if(users.isEmpty()) {
            return 1;
        }
        return users.getLast().getUserID() + 1;
    }

    private static void saveToFiles() throws IOException {

        try (ObjectOutputStream usersFile = new ObjectOutputStream(new FileOutputStream("saves/users.ser"))) {
            usersFile.writeObject(users);
        }
        System.out.println("Список пользователей сохранён в файл");

        try (ObjectOutputStream chatsFile = new ObjectOutputStream(new FileOutputStream("saves/chats.ser"))) {
            chatsFile.writeObject(chats);
        }
        System.out.println("Список чатов сохранён в файл");
    }

    private static void loadFromFiles() throws IOException, ClassNotFoundException {

        try (ObjectInputStream usersFile = new ObjectInputStream(new FileInputStream("saves/users.ser" ))) {
            users = (List<MessengerUser>) usersFile.readObject();
        }
        System.out.println("Список пользователей загружен из файла");

        try (ObjectInputStream chatsFile = new ObjectInputStream(new FileInputStream("saves/chats.ser"))) {
            chats = (List<Chat>) chatsFile.readObject();
        }
        System.out.println("Список чатов загружен из файла");
    }

    public static void main(String[] args) {
        // Создание и запуск серверной консоли
        ServerConsole serverConsole = new ServerConsole();
        serverConsole.start();

        // Попытка чтения списков пользователей
        // и чатов из файлов сохранения
        try {
            loadFromFiles();
        } catch(IOException e) {
            System.out.println("Не удалось прочитать файлы сохранения");
        } catch(ClassNotFoundException e) {
            System.err.println("ERROR: Непредвиденная ошибка");
            serverConsole.interrupt();
        }

        // Создание и запуск приёмника соединений
        ConnectionListener connectionListener = new ConnectionListener();
        connectionListener.start();

        // Ожидание завершения процесса консоли
        try {
            serverConsole.join();
        } catch(InterruptedException e) {
            System.err.println("ERROR: Непредвиденный Interrupted Exception");
            serverConsole.interrupt();
        }

        // Сохранение списков пользователей и чатов в файлы
        try {
            saveToFiles();
        } catch(IOException e) {
            System.err.println("ERROR: Ошибка сохранения состояния в файлы!");
        }
        connectionListener.close();
    }
}
