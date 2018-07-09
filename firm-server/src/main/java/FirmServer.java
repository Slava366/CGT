import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Сервер фирмы
 */
public class FirmServer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(FirmServer.class);                   // Лог

    private ExecutorService executorService = Executors.newFixedThreadPool(300);    // Потоки работы с запросами

    private Firm firm;                  // Фирма

    private ServerSocket server;        // Сервер

    private boolean exit = false;       // Флаг выхода


    /**
     * Конструктор
     * @param firm - фирма
     */
    public FirmServer(Firm firm) {
        // Создаем фирму с начальным капиталом
        this.firm = firm;
    }


    /**
     * @return - ServerSocket
     */
    public ServerSocket getServer() {
        return server;
    }


    /**
     * @return - деньги на счету в long
     */
    public long getMoney() {
        return firm.getMoney();
    }


    /**
     * @return - деньги на счету в double
     */
    public double getDoubleMoney() {
        return firm.getDoubleMoney();
    }


    /**
     * Устанавливает флаг завершения работы
     * @param exit - флаг
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }


    /**
     * Стартует сервер
     */
    @Override
    public void run() {
        // Стартуем сервер на порту 8080
        try {
            // Запускаем сервер на порту 8080
            server = new ServerSocket(8080);
            // Пока сервер на закрыт
            while (!server.isClosed()) {
                // Проверяем условие выхода
                if(exit) {
                    // Закрываем порт и выходим
                    server.close();
                    break;
                }
                // Ожидаем подключение
                Socket client = server.accept();
                // Отправляем в отдельную нить
                executorService.execute(new ServerHandler(firm, client));
            }
        } catch (IOException e) {
            if(!e.getMessage().equals("socket closed")) LOG.error(e.getMessage());
        }
    }
}
