package util;

import java.util.Scanner;

public class ServerConsole extends Thread {

    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {

        System.out.println("Сервер запущен по адресу: " + Main.address +
                ":" + Main.port);
        String str = scanner.nextLine();
        System.out.println(str);
        scanner.close();
    }
}
