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
        // Инициализтруем каналы общения
        try(ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            // Получаем запрос клиента
            Object requestObject = ois.readObject();
            // Определяем чей это запрос
            if(requestObject instanceof ProductRequest) {
                // Запрос заказчика
                // TODO
            }
            if(requestObject instanceof MaterialRequest) {
                // Отправляем ответ на запрос поставщика
                MaterialRequest materialRequest = (MaterialRequest) requestObject;
                Response response = new Response(false);
                long materialPrice = materialRequest.getPrice() * materialRequest.getAmount();
                if(firm.getMoney() >= materialPrice) {
                    firm.debitMoney(materialPrice);     // Снимаем деньги со счета
                    response.setSuccess(true);
                }
                // Сохраняем статистику
                ProviderStatistics providerStatistics = new ProviderStatistics();
                providerStatistics.setProviderName(materialRequest.getProviderName());
                providerStatistics.setMaterialName(materialRequest.getName());
                providerStatistics.setSale(response.isSuccess());
                providerStatistics.setPrice(materialPrice);
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
