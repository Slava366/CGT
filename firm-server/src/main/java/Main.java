import customer.CustomerStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.ProviderStatistics;
import response.StockMaterial;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);      // Лог

    private static Firm firm;       // Фирма


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
                if(stringMoney.matches("^[1-9][0-9]*((\\.|,)[0-9]+)?$")) {
                    // Если значение введено правильно
                    money = Math.round(Double.parseDouble(stringMoney.replaceAll(",", ".")) * 100);
                    break;
                }
            }
            if(!stringMoney.isEmpty()) System.out.println("Неверный формат данных! Пример - 12548,65 или 58964");
        }
        // Запускаем сервер фирмы
        LOG.info("Запуск сервера фирмы...");
        firm = new Firm(money);
        FirmServer firmServer = new FirmServer(firm);
        firmServer.start();
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
                System.out.printf("%-10s - количество материалов на складе\n", "materials");
                System.out.printf("%-10s - добавить материал\n", "madd");
                System.out.printf("%-10s - добавить продукт\n", "padd");
                System.out.printf("%-10s - завершение работы сервера\n", "exit");
                System.out.println();
                continue;
            }
            if(command.equalsIgnoreCase("money")) {
                System.out.printf("Текущее состояние счета - %.2f руб.\n", firmServer.getDoubleMoney());
                continue;
            }
            if(command.equalsIgnoreCase("stat")) { printStatistics(); continue; }
            if(command.equalsIgnoreCase("materials")) { printStockMaterials(); continue; }
            if(command.equalsIgnoreCase("madd")) continue;
            if(command.equalsIgnoreCase("padd")) continue;
            if(command.equalsIgnoreCase("exit")) break;
            if(!command.isEmpty()) System.out.printf("Неизвестная команда '%s'!\n", command);
        }
        // Завершаем работу сервера
        LOG.info("Завершение работы сервера...");
        try {
            firmServer.setExit(true);
            Thread.sleep(3000);
            if(!firmServer.getServer().isClosed()) firmServer.getServer().close();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {/**/}
        LOG.info("Сервер фирмы завершил работу!");
    }


    /**
     * Выводит статистику заказов
     */
    private static void printStatistics() {
        // Статистика по заказчикам
        List<CustomerStatistics> customerStatistics;
        try {
            customerStatistics = firm.getCustomerStatistics();
            System.out.println();
            if(0 != customerStatistics.size()) {
                System.out.println("Статистика по заказчикам:");
                System.out.printf("%-15s %-30s %-15s %-10s\n", "Наименование", "Продукт", "Сделка", "Цена, руб.");
            } else System.out.println("Статистика по заказчикам отсутствует!");
            for(CustomerStatistics cs : customerStatistics)
                System.out.printf("%-15s %-30s %-15s %-10.2f\n", cs.getCustomerName(), cs.getProductName(), (cs.isSale())? "Проведена" : "Отказано", (double) cs.getPrice() / 100);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        // Статистика по заказчикам
        List<ProviderStatistics> providerStatistics;
        try {
            providerStatistics = firm.getProviderStatistics();
            System.out.println();
            if(0 != providerStatistics.size()) {
                System.out.println("Статистика по поставщикам:");
                System.out.printf("%-15s %-30s %-15s %-10s\n", "Наименование", "Материал", "Сделка", "Цена, руб.");
            } else System.out.println("Статистика по поставщикам отсутствует!");
            for(ProviderStatistics ps : providerStatistics)
                System.out.printf("%-15s %-30s %-15s %-10.2f\n", ps.getProviderName(), ps.getMaterialName(), (ps.isSale())? "Проведена" : "Отказано", (double) ps.getPrice() / 100);
            System.out.println();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * Выводит остаток материалов на складе
     */
    private static void printStockMaterials() {
        List<StockMaterial> stockMaterials;
        try {
            stockMaterials = firm.getMaterials();
            System.out.println();
            if(0 != stockMaterials.size()) {
                System.out.println("Количество материалов на складе:");
                System.out.printf("%-5s %-30s %-15s %-10s\n", "Id", "Наименование", "Количество, шт.", "Цена, руб.");
            } else  System.out.println("На складе нет материалов!");
            for(StockMaterial material : stockMaterials)
                System.out.printf("%-5d %-30s %-15d %-10.2f\n", material.getId(), material.getName(), material.getAmount(), (double) material.getPrice() / 100);
            System.out.println();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }
}
