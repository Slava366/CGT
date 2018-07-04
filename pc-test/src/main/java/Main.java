import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);  // Лог

    private static Connection connection = null;                            // Активное соединение


    public static void main(String[] args) {
        LOG.info("Подготовка теста к запуску...");
        // Создаем сканер для прослушивания входного потока
        Scanner scanner = new Scanner(System.in);
        // Запрашиваем количество одновременно работающих заказчиков
        int customerAmount = 0;
        System.out.println("Необходимо указать количество одновременно работающих заказчиков!");
        while (true) {
            System.out.print("Введите количество одновременно работающих заказчиков >>> ");
            String stringAmount = "";
            if(scanner.hasNextLine()) {
                stringAmount = scanner.nextLine();
                stringAmount = stringAmount.trim();
                if(stringAmount.matches("^[1-9][0-9]{1,2}$")) {
                    // Если значение введено правильно
                    customerAmount = (int) Math.round(Double.parseDouble(stringAmount));
                    break;
                }
            }
            if(!stringAmount.isEmpty()) System.out.println("Неверный формат данных! Количество заказчиков не может превышать 999 шт.!");
        }
        LOG.info(String.format("Количество одновременно работающих заказчиков - %d", customerAmount));
        System.out.println();
        // Запрашиваем количество максимально допустимых отказов заказчику
        int maxFailure = 0;
        System.out.println("Необходимо указать количество максимально допустимых отказов заказчику!");
        while (true) {
            System.out.print("Введите количество максимально допустимых отказов заказчику >>> ");
            String stringMaxFailure = "";
            if(scanner.hasNextLine()) {
                stringMaxFailure = scanner.nextLine();
                stringMaxFailure = stringMaxFailure.trim();
                if(stringMaxFailure.matches("^[1-9][0-9]{1,2}$")) {
                    // Если значение введено правильно
                    maxFailure = (int) Math.round(Double.parseDouble(stringMaxFailure));
                    break;
                }
            }
            if(!stringMaxFailure.isEmpty()) System.out.println("Неверный формат данных! Количество максимально допустимых отказов заказчику не может превышать 999 раз!");
        }
        LOG.info(String.format("Количество максимально допустимых отказов заказчику - %d", maxFailure));
        System.out.println();
        // Запрашиваем размер начального капитала заказчика
        long customerMoney = 0;
        System.out.println("Неоходимо указать начальный капитал заказчика (общий для всех)!");
        while (true) {
            System.out.print("Введите начальный капитал заказчика >>> ");
            String stringMoney = "";
            if(scanner.hasNextLine()) {
                stringMoney = scanner.nextLine();
                stringMoney = stringMoney.trim();
                if(stringMoney.matches("^[1-9]+((\\.|,)[0-9]+)?$")) {
                    // Если значение введено правильно
                    customerMoney = Math.round(Double.parseDouble(stringMoney.replaceAll(",", ".")) * 100);
                    break;
                }
            }
            if(!stringMoney.isEmpty()) System.out.println("Неверный формат данных! Пример - 12548,65 или 58964");
        }
        LOG.info(String.format("Начальный капитал заказчика (общий для всех) - %.2f руб.",(double) customerMoney / 100));
        System.out.println();

        // Устанавливаем соединение с БД
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./db_firm");
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }

        // Начинаем работу заказчиков
        Customer customer = new Customer(954556, 51);
        System.out.println(customer.getNameId());;
    }
}
