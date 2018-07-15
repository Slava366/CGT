import customer.ProductRequest;
import firm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;


public class Customer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Customer.class);

    private Connection connection;               // Активное соединение с БД

    private static AtomicInteger id = new AtomicInteger(0);

    private Bill bill = new Bill();              // Счет

    private int maxFailure;                      // Максимальное число отказов

    private  AtomicInteger failure = new AtomicInteger(0);  // Отказы

    private int trying = 0;                     // Попытки заказать продукт


    /**
     * Конструктор
     * @param money - начальный капитал
     * @param maxFailure - максимальное количество отказов
     */
    public Customer(long money, int maxFailure, Connection connection) {
        bill.setMoney(money);
        this.maxFailure = maxFailure;
        this.connection = connection;
    }


    @Override
    public void run() {
        super.run();
        Thread.currentThread().setName(String.format("Заказчик_%03d", id.incrementAndGet()));
        LOG.info(String.format("Заказчик '%s' начал работу!", this.getName()));
        // Пока не кончились деньги или не получен предел отказов
        while (0 < bill.getMoney() & maxFailure > failure.get()) {
            // Получаем случайный продукт
            ProductRequest productRequest;
            try {
                productRequest = getRandProductRequest();
                if (null == productRequest) {
                    LOG.error("В БД фирмы отсутствуют продукты!");
                    break;
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                break;
            }
            // Выходим если не хватает денег
            if(productRequest.getPrice() * productRequest.getAmount() > bill.getMoney()) {
                if (10 < ++trying) continue;    // Пробуем заказать продукт, на который хватит денег
                break;  // Выходим
            }
            // Соединяемся с сервером фирмы и создаем потоки ввода/вывода
            try(Socket client = new Socket("localhost", 8080);
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                // Отправляем запрос
                oos.writeObject(productRequest);
                oos.flush();
                // Ждем ответ
                Object responseObject = ois.readObject();
                if(responseObject instanceof Response) {
                    if(((Response) responseObject).isSuccess()) {
                        bill.debitMoney(productRequest.getPrice() * productRequest.getAmount());     // Списываем деньги
                        LOG.info(String.format("Фирма продала продукт '%s' стоимостью %.2f руб. в количестве %d шт.",
                                productRequest.getName(), (double) productRequest.getPrice() / 100, productRequest.getAmount()));
                    } else {
                        failure.incrementAndGet();      // Увеличиваем счетчик отказов
                        LOG.info(String.format("Фирма отказала в продаже продукта '%s' стоимостью %.2f руб. в количестве %d шт.",
                                productRequest.getName(), (double) productRequest.getPrice() / 100, productRequest.getAmount()));
                    }
                } else {
                    LOG.error("Неизвестный ответ сервера!");
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                LOG.error(e.getMessage());
                break;
            }
        }
        LOG.info(String.format("Заказчик '%s' завершил свою работу!", this.getName()));
    }


    /**
     * @return - случайный продукт или null если в базе нет продуктов
     * @throws SQLException -
     */
    public ProductRequest getRandProductRequest() throws SQLException {
        String sql = "select products.id, products.name, sum(relation.amount * materials.price) as price " +
                "from products " +
                "inner join relation " +
                "on products.id = relation.pid " +
                "inner join materials " +
                "on relation.mid = materials.id " +
                "group by products.name " +
                "order by RAND() limit 1";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(sql);
        ProductRequest productRequest = null;
        if (resultSet.next()) {
            productRequest = new ProductRequest();
            productRequest.setId(resultSet.getInt("id"));
            productRequest.setCustomerName(this.getName());
            productRequest.setName(resultSet.getString("name"));
            productRequest.setPrice(resultSet.getLong("price"));
            productRequest.setAmount((int) (Math.random()*5) + 1);
        }
        return productRequest;
    }
}
