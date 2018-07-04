import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Фирма
 */
public class Firm {

    private static final Logger LOG = LoggerFactory.getLogger(Firm.class);

    private Bill bill = new Bill();      // Счет

    private Connection connection;       // Активное соединение с БД


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
        // Устанавливаем соединение с БД
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./db_firm");
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
        }
        // Создаем таблицы
        // Производимые товары
        String sql_table_products = "CREATE TABLE IF NOT EXISTS products " +
                "(" +
                "id integer auto_increment PRIMARY KEY, " +
                "name varchar(254) UNIQUE NOT NULL" +
                ")";
        // Материалы на складе
        String sql_table_materials = "CREATE TABLE IF NOT EXISTS materials " +
                "(" +
                "id integer auto_increment PRIMARY KEY, " +
                "name varchar(254) UNIQUE NOT NULL, " +
                "amount integer NOT NULL, " +
                "price bigint NOT NULL" +
                ")";
        // Зависимость товаров от материалов
        String sql_table_relation = "CREATE TABLE IF NOT EXISTS relation " +
                "(" +
                "pid integer NOT NULL, " +
                "mid integer NOT NULL, " +
                "amount integer NOT NULL" +
                ")";
        // Статистика запросов
        String sql_table_statistics_drop = "DROP TABLE IF EXISTS statistics";
        String sql_table_statistics = "CREATE TABLE IF NOT EXISTS statistics " +
                "(" +
                "message varchar(254) NOT NULL, " +
                "time timestamp NOT NULL" +
                ")";
        // Выволняем запрос по созданию таблиц
        try {
            Statement statement = connection.createStatement();
            statement.addBatch(sql_table_products);
            statement.addBatch(sql_table_materials);
            statement.addBatch(sql_table_relation);
            statement.addBatch(sql_table_statistics_drop);
            statement.addBatch(sql_table_statistics);
            statement.executeBatch();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * @return - текущее состояние счета
     */
    public long getMoney() {
        return bill.getMoney();
    }


    /**
     * @return - текущее состояние счета
     */
    public double getDoubleMoney() {
        return bill.getDoubleMoney();
    }
}
