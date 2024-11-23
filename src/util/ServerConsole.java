package util;

import java.util.Scanner;

public class ServerConsole extends Thread {

    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {

        System.out.println("Сервер запущен по адресу: " + Main.address +
                ":" + Main.port);

        while(true) {
            String str = scanner.nextLine();

            if(str.equals("stop")) {
                System.out.println("Остановка сервера...");
                break;
            } else {
                System.err.println("Неизвестная команда");
            }
        }
        scanner.close();
    }
}
