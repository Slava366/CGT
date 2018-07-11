import firm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.MaterialRequest;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class Provider extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Provider.class);

    private static AtomicInteger id = new AtomicInteger(0);

    private Connection connection;               // Активное соединение с БД

    private int interval;                        // Интервал между обращениями

    private boolean exit = false;                // Флаг выхода


    /**
     * Конструктор
     * @param interval - интервал между обращениями
     */
    public Provider(int interval, Connection connection) {
        this.interval = interval;
        this.connection = connection;
    }


    @Override
    public void run() {
        super.run();
        Thread.currentThread().setName(String.format("Поставщик_%03d", id.incrementAndGet()));
        LOG.info(String.format("Поставщик '%s' начал работу!", this.getName()));
        while (!exit) {
            // Получаем случайный материал
            MaterialRequest materialRequest;
            try {
                // Ждем заданный интервал
                Thread.sleep(interval);
                materialRequest = getRandMaterialRequest();
                if (null == materialRequest) {
                    LOG.error("В БД фирмы отсутствуют материалы!");
                    break;
                }
            } catch (InterruptedException | SQLException e) {
                LOG.error(e.getMessage());
                break;
            }
            // Соединяемся с сервером фирмы и создаем потоки ввода/вывода
            try(Socket client = new Socket("localhost", 8080);
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                // Отправляем запрос
                oos.writeObject(materialRequest);
                oos.flush();
                // Ждем ответ
                Object responseObject = ois.readObject();
                if(responseObject instanceof Response) {
                    if(((Response) responseObject).isSuccess()) LOG.info(String.format("Фирма купила метериал '%s' стоимостью %.2f руб. в количестве %d шт.",
                            materialRequest.getName(), (double) materialRequest.getPrice() / 100, materialRequest.getAmount()));
                    else LOG.info(String.format("Фирма отказала в покупке материала '%s' стоимостью %.2f руб. в количестве %d шт.",
                            materialRequest.getName(), (double) materialRequest.getPrice() / 100, materialRequest.getAmount()));
                } else {
                    LOG.error("Неизвестный ответ сервера!");
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                LOG.error(e.getMessage());
                break;
            }
        }
        LOG.info(String.format("Поставщик '%s' завершил свою работу!", this.getName()));
    }


    /**
     * Указывает поставщику, что пора завершать работу
     */
    public void exit() {
        this.exit = true;
    }


    /**
     * @return - случайный материал или null если в базе нет материалов
     * @throws SQLException -
     */
    public MaterialRequest getRandMaterialRequest() throws SQLException {
        String sql = "select * from materials order by RAND() limit 1";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        MaterialRequest materialRequest = null;
        if (resultSet.next()) {
            materialRequest = new MaterialRequest();
            materialRequest.setId(resultSet.getInt("id"));
            materialRequest.setProviderName(this.getName());
            materialRequest.setName(resultSet.getString("name"));
            materialRequest.setPrice(resultSet.getLong("price"));
            materialRequest.setAmount((int) (Math.random()*10) + 1);
        }
        return materialRequest;
    }
}
