package connection;

import messages.MessengerUser;
import util.ConnectionListener;
import util.Main;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class UnauthorizedConnection extends Connection {

    // Сканнер из потока ввода сокета
    private final Scanner scanner = new Scanner(in);
    // Флаг завершения цикла обработки команд
    private boolean isRunning = true;

    public UnauthorizedConnection(Socket socket) throws IOException {
        // Вызов конструктора класса Connection
        super(socket);
    }

    private String hashPassword(String password) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    // Метод-обработчик команды "login <username> <password>"
    private void login(String username, String password) {

        // Поиск пользователя с именем username в списке пользователей
        for(MessengerUser user : Main.users) {
            // Если пользователь найден
            if(user.getUserName().equals(username)) {
                // Но не совпали пароли
                if(!user.getHashedPassword().equals(hashPassword(password))) {
                    // Выдать ошибку "Invalid password"
                    try {
                        out.write("Invalid password\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch(IOException e) {
                        System.err.println("ERROR: Непредвиденная ошибка ввода-вывода в методе login");
                        close();
                    }
                    return;
                }

                // Иначе - авторизация успешная
                try {
                    out.write("OK\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();

                    System.out.println("Успешно выполнен вход пользователя " + username);
                } catch(IOException e) {
                    System.err.println("ERROR: Непредвиденная ошибка ввода-вывода при создании " +
                            "авторизованного соединения");
                    close();
                    return;
                }

                // Создание авторизованного соединения
                AuthorizedConnection authorizedConnection;
                try {
                    authorizedConnection = new AuthorizedConnection(socket, in, out, user);
                } catch(IOException e) {
                    System.err.println("ERROR: Непредвиденная ошибка ввода-вывода при создании " +
                            "авторизованного соединения");
                    close();
                    return;
                }
                // Завершение неавторизованного соединения
                ConnectionListener.getUnauthorizedConnections().remove(this);
                ConnectionListener.getAuthorizedConnections().add(authorizedConnection);
                authorizedConnection.start();
                isRunning = false;
                return;
            }
        }

        // Если пользователь не найден - выдать ошибку "Invalid username"
        try {
            out.write("Invalid username\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

        } catch(IOException e) {
            System.err.println("ERROR: Непредвиденная ошибка ввода-вывода в методе login");
            close();
        }
    }

    private void createUser(String username, String password) {
        for(MessengerUser user : Main.users) {
            if(user.getUserName().equals(username)) {
                try {
                    out.write("User already exists\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        try {
            out.write("OK\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            System.out.println("Успешно создан пользователь " + username);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        int id = Main.getNextUserID();
        String hashedPassword = hashPassword(password);
        MessengerUser newUser = new MessengerUser(id, username, hashedPassword);
        Main.users.add(newUser);
        login(username, password);
    }

    @Override
    public void run() {
        try {
            while(isRunning) {
                // Получение команды от клиента
                String command = scanner.next();
                String username;
                String password;
                // Распознавание команды
                switch(command) {
                    case "createUser":
                        username = scanner.next();
                        password = scanner.next();
                        // Запуск метода-обработчика
                        createUser(username, password);
                        break;
                    case "login":
                        username = scanner.next();
                        password = scanner.next();
                        login(username, password);
                        break;
                    case "disconnect":
                        isRunning = false;
                        close();
                        break;
                }
            }
        } catch(RuntimeException e) {
            System.out.println("Соединение с клиентом было разорвано!");
            close();
        }
    }
}
