import firm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
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
        int customerAmount = getCustomerAmount(scanner);
        // Запрашиваем количество максимально допустимых отказов заказчику
        int maxFailure = getCustomerMaxFailure(scanner);
        // Запрашиваем размер начального капитала заказчика
        long customerMoney = getCustomerMoney(scanner);
        // Запрашиваем количество одновременно работающих поставщиков
        int providerAmount = getProviderAmount(scanner);
        // Запрашиваем интервал между обращениями поставщика к серверу фирмы
        int providerInterval = getProviderInterval(scanner);

        // Устанавливаем соединение с БД
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./db_firm;AUTO_SERVER=true", "test", "pass");
            // Очищаем таблицы статистик
            String sql = "delete from customer; delete from provider;";
            Statement statement = connection.createStatement();
            statement.execute(sql);
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
        // Начинаем работу заказчиков
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
        }
        // Завершение работы поставщиков
        for(Provider provider : providers) {
            provider.exit();
            try {
                provider.join();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }
        }
        LOG.info("Тест завершил свою работу!");
        System.out.println();

        try {
            // Выводим статистику заказов по клиентам
            printCustomerStatistics(customerMoney);
            System.out.println();
            // Выводим статистику заказов по товарам
            printProductStatistics();
            System.out.println();
            // Выводим остатки материалов на складе
            printStockMaterials();
            System.out.println();
            // Остаток на счету фирмы
            printFirmMoney();
            System.out.println();
            // Закрываем соединение с БД
            if(!connection.isClosed()) connection.close();
            LOG.info("Соединение с БД закрыто!");
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        scanner.nextLine();
    }


    /**
     * Выводит остаток на счету фирмы
     */
    private static void printFirmMoney() {
        try(Socket client = new Socket("localhost", 8080);
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            oos.writeObject(new Response(true));
            oos.flush();
            Object response = ois.readObject();
            if(response instanceof Response) {
                System.out.printf("Остаток на счету фирмы: %.2f руб.\n", (double) ((Response) response).getMoney() / 100);
            } else LOG.error("Неизвестный ответ сервера!");
        } catch (IOException | ClassNotFoundException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * Выводит остатки материалов на складе
     */
    private static void printStockMaterials() throws SQLException {
        String sql = "select * from materials";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        boolean header = true;
        long sum = 0;
        while(resultSet.next()) {
            if(header) {
                System.out.println("На складе остались следующие материалы:");
                System.out.printf("%-30s %-20s %-20s\n", "Наименование", "Количество, шт.", "Стоимость, руб.");
                header = false;
            }
            System.out.printf("%-30s %-20d %-20.2f\n",
                    resultSet.getString("name"),
                    resultSet.getInt("amount"),
                    (double) resultSet.getLong("price") / 100);
            sum += resultSet.getInt("amount") * resultSet.getLong("price");
        }
        if(0 < sum) System.out.printf("На общую сумму: %.2f руб.\n", (double) sum / 100);
    }


    /**
     * Выводит статистику заказов по товарам
     */
    private static void printProductStatistics() throws SQLException {
        String sql = "select products.name as name, " +
                "count(*) as orderAmount, " +
                "sum(case when customer.sale = 1 then customer.amount else 0 end) as saleAmount, " +
                "sum(case when customer.sale = 1 then customer.price else 0 end) as price " +
                "from customer " +
                "inner join products " +
                "on customer.pid = products.id " +
                "group by customer.pid";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        boolean header = true;
        while(resultSet.next()) {
            if(header) {
                System.out.println("Статистика заказов по товарам:");
                System.out.printf("%-30s %-24s %-20s %-20s\n", "Наименование", "Количество заказов, шт.", "Продано, шт.", "На сумму, руб.");
                header = false;
            }
            System.out.printf("%-30s %-24d %-20d %-20.2f\n",
                    resultSet.getString("name"),
                    resultSet.getInt("orderAmount"),
                    resultSet.getLong("saleAmount"),
                    (double) resultSet.getLong("price") / 100);
        }
    }


    /**
     * Выводит статистику заказов по клиентам
     */
    private static void printCustomerStatistics(long customerMoney) throws SQLException {
        String sql = "select name, sum(price) as price from customer where sale = 1 group by name";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        boolean header = true;
        while(resultSet.next()) {
            if(header) {
                System.out.println("Статистика заказов по клиентам:");
                System.out.printf("%-30s %-20s %-20s\n", "Идентификатор", "Сумма заказов, руб.", "Остаток, руб.");
                header = false;
            }
            System.out.printf("%-30s %-20.2f %-20.2f\n",
                    resultSet.getString("name"),
                    (double) resultSet.getLong("price") / 100,
                    (double) (customerMoney - resultSet.getLong("price")) / 100);
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
                if(stringMoney.matches("^[1-9][0-9]*(([.,])[0-9]+)?$")) {
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
