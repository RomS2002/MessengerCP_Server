package util;

import connection.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionListener extends Thread {

    private ServerSocket serverSocket;
    private static final CopyOnWriteArrayList<UnauthorizedConnection> unauthorizedConnections
            = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<AuthorizedConnection> authorizedConnections
            = new CopyOnWriteArrayList<>();

    private final Timer clearFinishedConnections;

    private boolean isRunning = true;

    public ConnectionListener() {
        try {
            serverSocket = new ServerSocket(Main.port);
        } catch(IOException e) {
            System.err.println("ERROR: Ошибка открытия слушающего сокета");
            System.exit(100);
        }

        clearFinishedConnections = new Timer();
        clearFinishedConnections.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<UnauthorizedConnection> iter1 = unauthorizedConnections.iterator();
                while(iter1.hasNext()) {
                    Connection conn = iter1.next();
                    if(!conn.isAlive()) {
                        unauthorizedConnections.remove(conn);
                        System.out.println("Отключение неавторизованного клиента");
                    }
                }
                Iterator<AuthorizedConnection> iter2 = authorizedConnections.iterator();
                while(iter2.hasNext()) {
                    AuthorizedConnection conn = iter2.next();
                    if(!conn.isAlive()) {
                        authorizedConnections.remove(conn);
                        System.out.println("Отключение пользователя " + conn.getUser().getUserName());
                    }
                }
            }
        }, 0, 300);
    }

    @Override
    public void run() {

        while(isRunning) {

            Socket socket;
            try {
                socket = serverSocket.accept();
                System.out.println("Подключение нового клиента");
                UnauthorizedConnection connection = new UnauthorizedConnection(socket);
                unauthorizedConnections.add(connection);
                connection.start();

            } catch(Exception e) {
                System.out.println("Серверный сокет закрыт");
            }
        }
    }

    public synchronized void close() {
        isRunning = false;
        for(Connection conn : unauthorizedConnections) {
            conn.close();
        }
        for(Connection conn : authorizedConnections) {
            conn.close();
        }
        try {
            serverSocket.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        clearFinishedConnections.cancel();
    }

    public static CopyOnWriteArrayList<AuthorizedConnection> getAuthorizedConnections() {
        return authorizedConnections;
    }

    public static CopyOnWriteArrayList<UnauthorizedConnection> getUnauthorizedConnections() {
        return unauthorizedConnections;
    }
}
