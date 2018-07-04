import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);      // Лог

    public static void main(String[] args) {
        // Создаем сканер для прослушивания входного потока
        Scanner scanner = new Scanner(System.in);
        // Запускаем сервер фирмы
        LOG.info("Запуск сервера фирмы...");
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new ServerHandler());     // Обработчик запросов
            server.setExecutor(null);
            server.start();
            LOG.info("Сервер фирмы запущен!");
        } catch (IOException e) {
            // Ошибка запуска
            LOG.error(e.getMessage());
        }
        // Слушаем команды сервера
        System.out.println("Для получения списка доступных команд введите help. ");
        String command = "";
        while (true) {
            System.out.print("Введите команду >>> ");
            if(scanner.hasNextLine()) command = scanner.nextLine();
            if(command.equalsIgnoreCase("exit")) break;
            if(command.equalsIgnoreCase("help")) {
                System.out.println();
                System.out.println("Список доступных команд сервера:");
                System.out.printf("%-8s - завершение работы сервера\n", "exit");
                System.out.println();
                continue;
            }
            if(!command.isEmpty()) System.out.printf("Неизвестная команда '%s'!\n", command);
        }
        // Завершаем работу сервера
        LOG.info("Завершение работы сервера...");
        if(server != null) server.stop(0);
        LOG.info("Сервер фирмы завершил работу!");
    }
}
