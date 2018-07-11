package firm;

import java.io.Serializable;


/**
 * Отвер сервера
 */
public class Response implements Serializable {

    private boolean success;        // Статус

    private String message;         // Сообщение


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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
