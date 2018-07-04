/**
 * Фирма
 */
public class Firm {

    private Bill bill;      // Счет


    /**
     * Конструктор по умолчанию
     */
    public Firm() {
        this(0);
    }

    /**
     * Конструктор с указанием начального капитала
     * @param money - начальный капитал
     */
    public Firm(long money) {
        bill.setMoney(money);
    }


    /**
     * @return - текущее состояние счета
     */
    public long getMoney() {
        return bill.getMoney();
    }
}
