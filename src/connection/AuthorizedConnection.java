package connection;

import messages.MessengerUser;

import java.io.*;
import java.net.Socket;

public class AuthorizedConnection extends Connection {

    private MessengerUser user;

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
}
