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
     * @param message - сообщение
     */
    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
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
