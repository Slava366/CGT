import java.util.concurrent.atomic.AtomicLong;

/**
 * Счет
 */
public class Bill {

    private AtomicLong money = new AtomicLong(0);   // Количество денег на счету


    /**
     * @return - текущее количество денег
     */
    public long getMoney() {
        return money.get();
    }

    /**
     * @return - текущее количество денег
     */
    public double getDoubleMoney() {
        return money.doubleValue() / 100;
    }

    /**
     * Устанавливает новое значение денег
     * @param money - новое количество
     * @return - прежнее количество или -1
     */
    public long setMoney(long money) {
        if(money < 0) return -1;
        return this.money.getAndSet(money);
    }

    /**
     * Добавляет деньги на счет
     * @param money - количество денег
     * @return - прежнее количество или -1
     */
    public long addMoney(long money) {
        if(money < 0) return -1;
        return this.money.getAndAdd(money);
    }

    /**
     * Снимает деньги со счета
     * @param money - снимаемое количество
     * @return прежнее количество или -1
     */
    public long debitMoney(long money) {
        if(money < 0) return -1;
        return this.money.getAndSet(this.money.get() - money);
    }
}
