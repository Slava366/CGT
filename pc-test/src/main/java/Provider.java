import firm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.MaterialRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
     * @param interval - интервал между обращениями
     */
    public Provider(int interval, Connection connection) {
        this.interval = interval;
        this.connection = connection;
    }


    /**
     * Указывает поставщику, что пора завершать работу
     */
    public void exit() {
        this.exit = true;
    }

    @Override
    public void run() {
        super.run();
        Thread.currentThread().setName(String.format("Поставщик_%03d", id.incrementAndGet()));
        LOG.info(String.format("Поставщик '%s' начал работу!", this.getName()));
        while (!exit) {
            // Ждем заданный интервал
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
                break;
            }
            // Получаем случайный материал
            MaterialRequest materialRequest = null;
            try {
                Statement statement = connection.createStatement();
                String sql = "SELECT * FROM materials ORDER BY RAND() LIMIT 1";
                ResultSet result = statement.executeQuery(sql);
                if (result.next()) {
                    // Создаем запрос
                    materialRequest = new MaterialRequest();
                    materialRequest.setProviderName(this.getName());
                    materialRequest.setName(result.getString("name"));
                    materialRequest.setPrice(result.getLong("price"));
                    materialRequest.setAmount((int) (Math.random()*10) + 1);
                } else {
                    LOG.error("В БД фирмы отсутствуют материалы!");
                    break;
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                break;
            }
            // Соединяемся с сервером фирмы и создаем потоки ввода/вывода
            try {
                URL url = new URL("http://localhost:8080/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // Отправляем запрос
/*
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(is);
                System.out.println(br.readLine());
*/
                urlConnection.setDoOutput(true);
                OutputStreamWriter oos = new OutputStreamWriter(urlConnection.getOutputStream());
                BufferedWriter bw = new BufferedWriter(oos);
                bw.write("Привет!");
                bw.flush();
                urlConnection.setDoInput(true);
                InputStreamReader is = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(is);
                System.out.println(br.readLine());
                // Получаем ответ
//                ObjectInputStream ois = new ObjectInputStream(urlConnection.getInputStream());
//                Response response = (Response) ois.readObject();
//                ois.close();
//                LOG.info(String.format("На предложение о покупке материала '%s' фирма ответила %s", materialRequest.getName(), (response.isSuccess())? "согласием" : "отказом"));
                // Закрываем потоки
            } catch (IOException /*| ClassNotFoundException*/ e) {
                LOG.error(e.getMessage());
                break;
            }
        }
        LOG.info(String.format("Поставщик '%s' завершил свою работу!", this.getName()));
    }
}
