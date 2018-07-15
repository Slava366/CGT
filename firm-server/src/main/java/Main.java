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
                if(stringMoney.matches("^[1-9][0-9]*(([.,])[0-9]+)?$")) {
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
                System.out.printf("Текущее состояние счета: %.2f руб.\n", firmServer.getDoubleMoney());
                continue;
            }
            if(command.equalsIgnoreCase("stat")) { printStatistics(); continue; }
            if(command.equalsIgnoreCase("materials")) { printStockMaterials(); continue; }
            if(command.equalsIgnoreCase("madd")) { addStockMaterial(scanner); continue; }
            if(command.equalsIgnoreCase("padd")) { addStockProduct(scanner); continue; }
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
     * Просим ввести новый продукт
     * @param scanner - сканер входного потока
     */
    private static void addStockProduct(Scanner scanner) {
        while(true) {
            String inline = "";
            System.out.print("Введите продукт или Enter для отмены (пример: продукт/материал1/количество/цена/материал2/количество/цена) >>> ");
            if (scanner.hasNextLine()) inline = scanner.nextLine();
            if((inline = inline.trim()).equals("")) break;  // Введен Enter
            String[] material = inline.split("\\s*/\\s*");
            if(1 != material.length % 3) {
                System.out.println("Вы указали не полную информацию!");
                continue;  // Не вся информация введена
            }
            if (!material[0].matches("^[A-Za-zА-Яа-я]+[A-Za-zА-Яа-я -_]*[A-Za-zА-Яа-я]+$")) {
                System.out.println("Наименование продукта содержит недопустимые символы!");
                continue;    // Недопустимое название
            }
            boolean success = true;
            for (int i = 0; i < material.length / 3; i++) {
                if (!material[i * 3 + 1].matches("^[A-Za-zА-Яа-я]+[A-Za-zА-Яа-я -_]*[A-Za-zА-Яа-я]+$")) {
                    System.out.println("Наименование материала содержит недопустимые символы!");
                    success = false;    // Недопустимое название
                    break;
                }
                if(!material[i * 3 + 2].matches("^\\d+$")) {
                    System.out.println("Количество материала содержит недопустимые символы!");
                    success = false;    // Недопустимое количество
                    break;
                }
                if(!material[i * 3 + 3].matches("^\\d+([.,]\\d+)?$")) {
                    System.out.println("Цена материала содержит недопустимые символы!");
                    success = false;    // Недопустимая цена
                    break;
                }
            }
            if(!success) continue;  // Есть ошибки в введенной информации
            String productName = material[0].substring(0,1).toUpperCase() + material[0].substring(1);
            String materialName;
            int materialAmount;
            long materialPrice;
            for (int i = 0; i < material.length / 3; i++) {
                materialName = material[i * 3 + 1].substring(0,1).toUpperCase() + material[i * 3 + 1].substring(1);
                materialAmount = Integer.parseInt(material[i * 3 + 2]);
                materialPrice = Math.round(Double.parseDouble(material[i * 3 + 3].replaceAll(",", ".")) * 100);
                try {
                    firm.addStockProduct(productName, materialName, materialAmount, materialPrice);
                } catch (SQLException e) {
                    LOG.error(e.getMessage());
                }
            }
            LOG.info(String.format("Добавлен продукт '%s'.\n", productName));
            break;
        }
    }


    /**
     * Просим ввести новый материал
     * @param scanner - сканер входного потока
     */
    private static void addStockMaterial(Scanner scanner) {
        String materialName;
        int materialAmount;
        long materialPrice;
        while(true) {
            String inline = "";
            System.out.print("Введите материал или Enter для отмены (пример: наименование/количество/цена) >>> ");
            if (scanner.hasNextLine()) inline = scanner.nextLine();
            if((inline = inline.trim()).equals("")) break;  // Введен Enter
            String[] material = inline.split("\\s*/\\s*");
            if(3 > material.length) {
                System.out.println("Вы указали не полную информацию!");
                continue;  // Не вся информация введена
            }
            if (!material[0].matches("^[A-Za-zА-Яа-я]+[A-Za-zА-Яа-я -_]*[A-Za-zА-Яа-я]+$")) {
                System.out.println("Наименование содержит недопустимые символы!");
                continue;    // Недопустимое название
            }
            if(!material[1].matches("^\\d+$")) {
                System.out.println("Количество содержит недопустимые символы!");
                continue;    // Недопустимое количество
            }
            if(!material[2].matches("^\\d+([.,]\\d+)?$")) {
                System.out.println("Цена содержит недопустимые символы!");
                continue;    // Недопустимая цена
            }
            materialName = material[0].substring(0,1).toUpperCase() + material[0].substring(1);
            materialAmount = Integer.parseInt(material[1]);
            materialPrice = Math.round(Double.parseDouble(material[2].replaceAll(",", ".")) * 100);
            try {
                firm.addStockMaterial(materialName, materialAmount, materialPrice);
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }
            LOG.info(String.format("Добавлен материал '%s' в количестве %d, стоимостью %.2f\n", materialName, materialAmount, (double) materialPrice / 100));
            break;
        }
    }


    /**
     * Выводит статистику заказов
     */
    private static void printStatistics() {
        // Статистика по заказчикам
        List<CustomerStatistics> customerStatistics;
        long customerSum = 0;
        try {
            customerStatistics = firm.getCustomerStatistics();
            System.out.println();
            if(0 != customerStatistics.size()) {
                System.out.println("Статистика по заказчикам:");
                System.out.printf("%-15s %-30s %-15s %-15s %-10s\n", "Наименование", "Продукт", "Сделка", "Количество, шт.", "Сумма, руб.");
            } else System.out.println("Статистика по заказчикам отсутствует!");
            for(CustomerStatistics cs : customerStatistics) {
                if(cs.isSale()) customerSum += cs.getPrice();
                System.out.printf("%-15s %-30s %-15s %-15d %-10.2f\n", cs.getCustomerName(), cs.getProductName(), (cs.isSale())? "Проведена" : "Отказано", cs.getAmount(), (double) cs.getPrice() / 100);
            }
            System.out.printf("Итого продано продуктов на сумму: %.2f руб.\n", (double) customerSum / 100);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
        // Статистика по заказчикам
        List<ProviderStatistics> providerStatistics;
        long providerSum = 0;
        try {
            providerStatistics = firm.getProviderStatistics();
            System.out.println();
            if(0 != providerStatistics.size()) {
                System.out.println("Статистика по поставщикам:");
                System.out.printf("%-15s %-30s %-15s %-15s %-10s\n", "Наименование", "Материал", "Сделка", "Количество, шт.", "Сумма, руб.");
            } else System.out.println("Статистика по поставщикам отсутствует!");
            for(ProviderStatistics ps : providerStatistics) {
                if(ps.isSale()) providerSum += ps.getPrice();
                System.out.printf("%-15s %-30s %-15s %-15d %-10.2f\n", ps.getProviderName(), ps.getMaterialName(), (ps.isSale())? "Проведена" : "Отказано", ps.getAmount(), (double) ps.getPrice() / 100);
            }
            System.out.printf("Итого куплено материалов на сумму: %.2f руб.\n", (double) providerSum / 100);
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
