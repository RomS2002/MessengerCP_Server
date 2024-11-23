package connection;

import java.io.*;
import java.net.Socket;

public abstract class Connection extends Thread {

    protected Socket socket;

    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    public Connection() {

    }

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch(IOException ex) {
            System.err.println("ERROR: неопределённая ошибка");
            System.exit(100);
        }
    }
}
