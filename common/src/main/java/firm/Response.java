package firm;

import java.io.Serializable;


/**
 * Отвер сервера
 */
public class Response implements Serializable {

    private boolean success;        // Статус

    private long money;


    /**
     * Конструктор
     * @param success - статус
     *
     */
    public Response(boolean success) {
        this.success = success;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }
}
