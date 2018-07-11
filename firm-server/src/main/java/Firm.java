import customer.CustomerStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.ProviderStatistics;
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


    /**
     * @return - Статистика заказов по заказчику
     * @throws SQLException -
     */
    public List<CustomerStatistics> getCustomerStatistics() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        String sql = "select customer.name as customerName, products.name as productName, customer.sale, customer.price " +
                "from customer " +
                "inner join products " +
                "on customer.pid = products.id";
        ResultSet resultSet = statement.executeQuery(sql);
        List<CustomerStatistics> statistics = new LinkedList<>();
        while (resultSet.next()) {
            CustomerStatistics customerStatistics = new CustomerStatistics();
            customerStatistics.setCustomerName(resultSet.getString("customerName"));
            customerStatistics.setProductName(resultSet.getString("productName"));
            customerStatistics.setSale(resultSet.getBoolean("sale"));
            customerStatistics.setPrice(resultSet.getLong("price"));
            statistics.add(customerStatistics);
        }
        return statistics;
    }


    /**
     * @return - Статистика заказов по поставщику
     * @throws SQLException -
     */
    public List<ProviderStatistics> getProviderStatistics() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        String sql = "select provider.name as providerName, materials.name as materialName, provider.sale, provider.price " +
                "from provider " +
                "inner join materials " +
                "on provider.mid = materials.id";
        ResultSet resultSet = statement.executeQuery(sql);
        List<ProviderStatistics> statistics = new LinkedList<>();
        while (resultSet.next()) {
            ProviderStatistics providerStatistics = new ProviderStatistics();
            providerStatistics.setProviderName(resultSet.getString("providerName"));
            providerStatistics.setMaterialName(resultSet.getString("materialName"));
            providerStatistics.setSale(resultSet.getBoolean("sale"));
            providerStatistics.setPrice(resultSet.getLong("price"));
            statistics.add(providerStatistics);
        }
        return statistics;
    }
}
