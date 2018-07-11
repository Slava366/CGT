import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);  // Лог

    private static Connection connection = null;                            // Активное соединение


    public static void main(String[] args) {
        LOG.info("Подготовка теста к запуску...");
        // Создаем сканер для прослушивания входного потока
        Scanner scanner = new Scanner(System.in);
        // Запрашиваем количество одновременно работающих заказчиков
        int customerAmount = 5;//getCustomerAmount(scanner);
        // Запрашиваем количество максимально допустимых отказов заказчику
        int maxFailure = 5;//getCustomerMaxFailure(scanner);
        // Запрашиваем размер начального капитала заказчика
        long customerMoney = 5000;//getCustomerMoney(scanner);
        // Запрашиваем количество одновременно работающих поставщиков
        int providerAmount = 2;//getProviderAmount(scanner);
        // Запрашиваем интервал между обращениями поставщика к серверу фирмы
        int providerInterval = 2000;//getProviderInterval(scanner);

        // Устанавливаем соединение с БД
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./db_firm;AUTO_SERVER=true", "test", "pass");
            LOG.info("Установлено соединение с БД!");
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
            System.exit(1);
        }
        LOG.info("Запускаем тест...");

        // Начинаем работу поставщиков
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < providerAmount; i++) {
            Provider provider = new Provider(providerInterval, connection);
            providers.add(provider);
            provider.start();
        }
        /*// Начинаем работу заказчиков
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < customerAmount; i++) {
            Customer customer = new Customer(customerMoney, maxFailure, connection);
            customers.add(customer);
            customer.start();
        }
        System.out.println();

        // Завершение работы заказчиков
        for(Customer customer : customers) {
            try {
                customer.join();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }*/
        /*// Завершение работы поставщиков
        for(Provider provider : providers) {
            provider.exit();
            try {
                provider.join();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
        LOG.info("Тест завершил свою работу!");
        System.out.println();*/
        try {
            providers.get(0).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Закрываем соединение с БД
        try {
            if(!connection.isClosed()) connection.close();
            LOG.info("Соединение с БД закрыто!");
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * Запрашивает интервал между обращениями поставщика к серверу фирмы
     * @param scanner - сканер входного потока
     */
    private static int getProviderInterval(Scanner scanner) {
        int providerInterval = 0;
        System.out.println("Необходимо указать интервал между обращениями поставщика к серверу фирмы!");
        while (true) {
            System.out.print("Введите интервал между обращениями поставщика к серверу фирмы в мс >>> ");
            String stringAmount = "";
            if(scanner.hasNextLine()) {
                stringAmount = scanner.nextLine();
                stringAmount = stringAmount.trim();
                if(stringAmount.matches("^[1-9][0-9]{0,3}$")) {
                    // Если значение введено правильно
                    providerInterval = (int) Math.round(Double.parseDouble(stringAmount));
                    break;
                }
            }
            if(!stringAmount.isEmpty()) System.out.println("Неверный формат данных! Интервал между обращениями поставщика к серверу фирмы не может превышать 9999 мс!");
        }
        LOG.info(String.format("Интервал между обращениями поставщика к серверу фирмы - %d мс", providerInterval));
        System.out.println();
        return providerInterval;
    }


    /**
     * Запрашивает количество одновременно работающих поставщиков
     * @param scanner - сканер входного потока
     */
    private static int getProviderAmount(Scanner scanner) {
        int providerAmount = 0;
        System.out.println("Необходимо указать количество одновременно работающих поставщиков!");
        while (true) {
            System.out.print("Введите количество одновременно работающих поставщиков >>> ");
            String stringAmount = "";
            if(scanner.hasNextLine()) {
                stringAmount = scanner.nextLine();
                stringAmount = stringAmount.trim();
                if(stringAmount.matches("^[1-9][0-9]{0,2}$")) {
                    // Если значение введено правильно
                    providerAmount = (int) Math.round(Double.parseDouble(stringAmount));
                    break;
                }
            }
            if(!stringAmount.isEmpty()) System.out.println("Неверный формат данных! Количество поставщиков не может превышать 999 шт.!");
        }
        LOG.info(String.format("Количество одновременно работающих поставщиков - %d", providerAmount));
        System.out.println();
        return providerAmount;
    }


    /**
     * Запрашивает размер начального капитала заказчика
     * @param scanner - сканер входного потока
     */
    private static long getCustomerMoney(Scanner scanner) {
        long customerMoney = 0;
        System.out.println("Неоходимо указать начальный капитал заказчика (общий для всех)!");
        while (true) {
            System.out.print("Введите начальный капитал заказчика >>> ");
            String stringMoney = "";
            if(scanner.hasNextLine()) {
                stringMoney = scanner.nextLine();
                stringMoney = stringMoney.trim();
                if(stringMoney.matches("^[1-9][0-9]*((\\.|,)[0-9]+)?$")) {
                    // Если значение введено правильно
                    customerMoney = Math.round(Double.parseDouble(stringMoney.replaceAll(",", ".")) * 100);
                    break;
                }
            }
            if(!stringMoney.isEmpty()) System.out.println("Неверный формат данных! Пример - 12548,65 или 58964");
        }
        LOG.info(String.format("Начальный капитал заказчика (общий для всех) - %.2f руб.",(double) customerMoney / 100));
        System.out.println();
        return customerMoney;
    }


    /**
     * Запрашиваеn количество максимально допустимых отказов заказчику
     * @param scanner - сканер входного потока
     */
    private static int getCustomerMaxFailure(Scanner scanner) {
        int maxFailure = 0;
        System.out.println("Необходимо указать количество максимально допустимых отказов заказчику!");
        while (true) {
            System.out.print("Введите количество максимально допустимых отказов заказчику >>> ");
            String stringMaxFailure = "";
            if(scanner.hasNextLine()) {
                stringMaxFailure = scanner.nextLine();
                stringMaxFailure = stringMaxFailure.trim();
                if(stringMaxFailure.matches("^[1-9][0-9]{0,2}$")) {
                    // Если значение введено правильно
                    maxFailure = (int) Math.round(Double.parseDouble(stringMaxFailure));
                    break;
                }
            }
            if(!stringMaxFailure.isEmpty()) System.out.println("Неверный формат данных! Количество максимально допустимых отказов заказчику не может превышать 999 раз!");
        }
        LOG.info(String.format("Количество максимально допустимых отказов заказчику - %d", maxFailure));
        System.out.println();
        return maxFailure;
    }


    /**
     * Запрашивает количество одновременно работающих заказчиков
     * @param scanner - сканер входного потока
     */
    private static int getCustomerAmount(Scanner scanner) {
        int customerAmount = 0;
        System.out.println("Необходимо указать количество одновременно работающих заказчиков!");
        while (true) {
            System.out.print("Введите количество одновременно работающих заказчиков >>> ");
            String stringAmount = "";
            if(scanner.hasNextLine()) {
                stringAmount = scanner.nextLine();
                stringAmount = stringAmount.trim();
                if(stringAmount.matches("^[1-9][0-9]{0,2}$")) {
                    // Если значение введено правильно
                    customerAmount = (int) Math.round(Double.parseDouble(stringAmount));
                    break;
                }
            }
            if(!stringAmount.isEmpty()) System.out.println("Неверный формат данных! Количество заказчиков не может превышать 999 шт.!");
        }
        LOG.info(String.format("Количество одновременно работающих заказчиков - %d", customerAmount));
        System.out.println();
        return customerAmount;
    }
}
