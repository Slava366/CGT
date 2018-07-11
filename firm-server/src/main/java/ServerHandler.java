import customer.CustomerStatistics;
import customer.ProductRequest;
import firm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.MaterialRequest;
import provider.ProviderStatistics;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Обработчик запросов
 */
public class ServerHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);                   // Лог

    private Firm firm;          // Фирма

    private Socket client;      // Диалог с клиентом

    private final Object lock = new Object();


    /**
     * Конструктор
     * @param firm - фирма
     * @param client - диалог с клиентом
     */
    public ServerHandler(Firm firm, Socket client) {
        this.firm = firm;
        this.client = client;
    }


    /**
     * Общение с клиентом
     */
    @Override
    public void run() {
        synchronized (lock) {
            // Инициализтруем каналы общения
            try (ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                // Получаем запрос клиента
                Object requestObject = ois.readObject();
                // Определяем чей это запрос
                if (requestObject instanceof ProductRequest) {
                    // Отправляем ответ на запрос заказчика
                    ProductRequest productRequest = (ProductRequest) requestObject;
                    Response response = new Response(false);
                    long productPrice = productRequest.getPrice() * productRequest.getAmount();
                    // Проверяем может ли фирма произвести этот продукт
                    if (firm.canCreateProduct(productRequest.getId(), productRequest.getAmount())) {
                        // Удаляем продукт со склада
                        firm.productFromStock(productRequest.getId(), productRequest.getAmount());
                        firm.addMoney(productPrice);     // Кидаем деньги на счет
                        response.setSuccess(true);
                    }
                    // Сохраняем статистику
                    CustomerStatistics customerStatistics = new CustomerStatistics();
                    customerStatistics.setCustomerName(productRequest.getCustomerName());
                    customerStatistics.setProductName(productRequest.getName());
                    customerStatistics.setSale(response.isSuccess());
                    customerStatistics.setPrice(productPrice);
                    customerStatistics.setAmount(productRequest.getAmount());
                    firm.addCustomerStatistics(customerStatistics);
                    // Отправляем результат
                    oos.writeObject(response);
                    oos.flush();
                }
                if (requestObject instanceof MaterialRequest) {
                    // Отправляем ответ на запрос поставщика
                    MaterialRequest materialRequest = (MaterialRequest) requestObject;
                    Response response = new Response(false);
                    long materialPrice = materialRequest.getPrice() * materialRequest.getAmount();
                    if (firm.getMoney() >= materialPrice) {
                        // Добавляем материал на склад
                        firm.materialToStock(materialRequest.getId(), materialRequest.getAmount());
                        firm.debitMoney(materialPrice);     // Снимаем деньги со счета
                        response.setSuccess(true);
                    }
                    // Сохраняем статистику
                    ProviderStatistics providerStatistics = new ProviderStatistics();
                    providerStatistics.setProviderName(materialRequest.getProviderName());
                    providerStatistics.setMaterialName(materialRequest.getName());
                    providerStatistics.setSale(response.isSuccess());
                    providerStatistics.setPrice(materialPrice);
                    providerStatistics.setAmount(materialRequest.getAmount());
                    firm.addProviderStatistics(providerStatistics);
                    // Отправляем результат
                    oos.writeObject(response);
                    oos.flush();
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
