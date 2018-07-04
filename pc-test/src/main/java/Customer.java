import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Customer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Customer.class);

    private Connection connection;               // Активное соединение с БД

    private static AtomicInteger id = new AtomicInteger(0);

    private String name;                         // Название заказчика

    private Bill bill = new Bill();              // Счет

    private int maxFailure;                      // Максимальное число отказов


    /**
     * Конструктор
     * @param money - начальный капитал
     * @param maxFailure - максимальное количество отказов
     */
    public Customer(long money, int maxFailure) {
        bill.setMoney(money);
        this.maxFailure = maxFailure;
        name = String.format("Заказчик_%03d", id.incrementAndGet());
    }


    /**
     *
     * @return - наименование заказчика
     */
    public String getNameId() {
        return name;
    }


    @Override
    public void run() {
        super.run();
    }
}
