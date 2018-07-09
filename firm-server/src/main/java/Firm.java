import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.StockMaterial;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

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
            connection = DriverManager.getConnection("jdbc:h2:file:./db_firm;AUTO_SERVER=TRUE", "test", "pass");
            LOG.info("Установлено соединение с БД!");
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage());
            System.exit(1);
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


    /**
     * @return - остатки материалов на складе
     * @throws SQLException -
     */
    public List<StockMaterial> getMaterials() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        String sql = "select * from materials";
        ResultSet resultSet = statement.executeQuery(sql);
        List<StockMaterial> materials = new LinkedList<>();
        while (resultSet.next()) {
            StockMaterial material = new StockMaterial();
            material.setId(resultSet.getInt("id"));
            material.setName(resultSet.getString("name"));
            material.setAmount(resultSet.getInt("amount"));
            material.setPrice(resultSet.getLong("price"));
            materials.add(material);
        }
        return materials;
    }
}
