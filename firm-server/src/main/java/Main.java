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
        // Запрашиваем размер начального капитала фирмы
        long money = 0;
        System.out.println("Неоходимо указать начальный капитал фирмы!");
        while (true) {
            System.out.print("Введите начальный капитал фирмы >>> ");
            String stringMoney = "";
            if(scanner.hasNextLine()) {
                stringMoney = scanner.nextLine();
                stringMoney = stringMoney.trim();
                if(stringMoney.matches("^[1-9]+((\\.|,)[0-9]+)?$")) {
                    // Если значение введено правильно
                    money = Math.round(Double.parseDouble(stringMoney.replaceAll(",", ".")) * 100);
                    break;
                }
            }
            if(!stringMoney.isEmpty()) System.out.println("Неверный формат данных! Пример - 12548,65 или 58964");
        }
        // Создаем фирму
        Firm firm = new Firm(money);
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
            if(command.equalsIgnoreCase("help")) {
                System.out.println();
                System.out.println("Список доступных команд сервера:");
                System.out.printf("%-10s - отчет о состоянии счета\n", "money");
                System.out.printf("%-10s - статистика обработки заказов\n", "stat");
                System.out.printf("%-10s - количество материалов на складе\n", "material");
                System.out.printf("%-10s - завершение работы сервера\n", "exit");
                System.out.println();
                continue;
            }
            if(command.equalsIgnoreCase("money")) {
                System.out.printf("Текущее состояние счета - %.2f руб.\n", firm.getDoubleMoney());
                continue;
            }
            if(command.equalsIgnoreCase("stat")) continue;
            if(command.equalsIgnoreCase("material")) continue;
            if(command.equalsIgnoreCase("exit")) break;
            if(!command.isEmpty()) System.out.printf("Неизвестная команда '%s'!\n", command);
        }
        // Завершаем работу сервера
        LOG.info("Завершение работы сервера...");
        if(server != null) server.stop(0);
        LOG.info("Сервер фирмы завершил работу!");
    }
}
