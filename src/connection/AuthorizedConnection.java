package connection;

import messages.MessengerUser;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AuthorizedConnection extends Connection {

    private MessengerUser user;
    private Scanner scanner = new Scanner(in);
    private boolean isRunning = true;

    public AuthorizedConnection(Socket socket, ObjectInputStream in, ObjectOutputStream out,
                                MessengerUser user) throws IOException {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.user = user;
    }

    public MessengerUser getUser() {
        return user;
    }

    @Override
    public void run() {
        while(isRunning) {
            String command = scanner.next();
            String username;
            String password;

            switch(command) {
                case "getUsers":
                    username = scanner.next();
                    password = scanner.next();
                    createUser(username, password);
                    break;
                case "login":
                    username = scanner.next();
                    password = scanner.next();
                    login(username, password);
                    break;
            }
        }
    }
}
