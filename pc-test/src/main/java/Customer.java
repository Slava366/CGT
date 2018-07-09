import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

public class Customer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Customer.class);

    private Connection connection;               // Активное соединение с БД

    private static AtomicInteger id = new AtomicInteger(0);

    private Bill bill = new Bill();              // Счет

    private int maxFailure;                      // Максимальное число отказов


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

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOG.info(String.format("Заказчик '%s' завершил свою работу!", this.getName()));
    }
}
