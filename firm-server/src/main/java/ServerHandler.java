import customer.ProductRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.MaterialRequest;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик запросов
 */
public class ServerHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);                   // Лог

    private Socket client;


    /**
     * Конструктор
     * @param client - диалог с клиентом
     */
    public ServerHandler(Socket client) {
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
            }
            if(requestObject instanceof MaterialRequest) {
                // Запрос поставщика

            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.error(e.getMessage());
        }
    }
}
