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
     * Снимает деньги
     * @param money - снимаемая сумма
     * @return - прежняя сумма или -1
     */
    public long debitMoney(long money) {
        return bill.debitMoney(money);
    }


    /**
     * Устанавливает новое значение денег
     * @param money - новое количество
     * @return - прежнее количество или -1
     */
    public long setMoney(long money) {
        return bill.setMoney(money);
    }


    /**
     * Добавляет деньги на счет
     * @param money - количество денег
     * @return - прежнее количество или -1
     */
    public long addMoney(long money) {
        return bill.addMoney(money);
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
        String sql = "select customer.name as customerName, products.name as productName, customer.amount, customer.sale, customer.price " +
                "from customer " +
                "inner join products " +
                "on customer.pid = products.id";
        ResultSet resultSet = statement.executeQuery(sql);
        List<CustomerStatistics> statistics = new LinkedList<>();
        while (resultSet.next()) {
            CustomerStatistics customerStatistics = new CustomerStatistics();
            customerStatistics.setCustomerName(resultSet.getString("customerName"));
            customerStatistics.setProductName(resultSet.getString("productName"));
            customerStatistics.setAmount(resultSet.getInt("amount"));
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
        String sql = "select provider.name as providerName, materials.name as materialName, provider.amount, provider.sale, provider.price " +
                "from provider " +
                "inner join materials " +
                "on provider.mid = materials.id";
        ResultSet resultSet = statement.executeQuery(sql);
        List<ProviderStatistics> statistics = new LinkedList<>();
        while (resultSet.next()) {
            ProviderStatistics providerStatistics = new ProviderStatistics();
            providerStatistics.setProviderName(resultSet.getString("providerName"));
            providerStatistics.setMaterialName(resultSet.getString("materialName"));
            providerStatistics.setAmount(resultSet.getInt("amount"));
            providerStatistics.setSale(resultSet.getBoolean("sale"));
            providerStatistics.setPrice(resultSet.getLong("price"));
            statistics.add(providerStatistics);
        }
        return statistics;
    }


    /**
     * Сохраняет сделку с заказчиком
     * @param customerStatistics - сделка с заказчиком
     * @throws SQLException -
     */
    public void addCustomerStatistics(CustomerStatistics customerStatistics) throws SQLException {
        String sql = "insert into customer values(?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, customerStatistics.getCustomerName());
        statement.setInt(2, getProductId(customerStatistics.getProductName()));
        statement.setInt(3, customerStatistics.getAmount());
        statement.setBoolean(4, customerStatistics.isSale());
        statement.setLong(5, customerStatistics.getPrice());
        statement.executeUpdate();
    }


    /**
     * Возвращает идентификатор продукта
     * @param productName - название продукта
     * @return - идентификатор материала
     * @throws SQLException -
     */
    private int getProductId(String productName) throws SQLException {
        String sql = "select * from products where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, productName);
        ResultSet resultSet = statement.executeQuery();
        int id = 0;
        if(resultSet.next()) id = resultSet.getInt("id");
        return id;
    }


    /**
     * Сохраняет сделку с поставщиком
     * @param providerStatistics - сделка с поставщиком
     * @throws SQLException -
     */
    public void addProviderStatistics(ProviderStatistics providerStatistics) throws SQLException {
        String sql = "insert into provider values(?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, providerStatistics.getProviderName());
        statement.setInt(2, getMaterialId(providerStatistics.getMaterialName()));
        statement.setInt(3, providerStatistics.getAmount());
        statement.setBoolean(4, providerStatistics.isSale());
        statement.setLong(5, providerStatistics.getPrice());
        statement.executeUpdate();
    }


    /**
     * Возвращает идентификатор материала
     * @param materialName - название материала
     * @return - идентификатор материала
     * @throws SQLException -
     */
    public int getMaterialId(String materialName) throws SQLException {
        String sql = "select * from materials where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, materialName);
        ResultSet resultSet = statement.executeQuery();
        int id = 0;
        if(resultSet.next()) id = resultSet.getInt("id");
        return id;
    }


    /**
     * Может ли фирма произвести продукт с идентификатором id
     * @param id - идентификатор продукта
     * @param amount - количество продукта
     * @return - true может
     */
    public boolean canCreateProduct(int id, int amount) throws SQLException {
        String sql = "select * " +
                "from products " +
                "inner join relation " +
                "on products.id = relation.pid " +
                "inner join materials " +
                "on relation.mid = materials.id " +
                "where products.id = ? and materials.amount >= relation.amount * ? " +
                "group by products.name, materials.name";
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, id);
        statement.setInt(2, amount);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }


    /**
     * Добавляет материал на склад
     * @param id - идентификатор материала
     * @param amount - количество материала
     */
    public void materialToStock(int id, int amount) throws SQLException {
        String sql = "update materials set materials.amount = materials.amount + ? where materials.id = ?";
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, amount);
        statement.setInt(2, id);
        statement.executeUpdate();
    }


    /**
     * Удаляет материал со склада
     * @param id - идентификатор материала
     * @param amount - количество материала
     */
    public void productFromStock(int id, int amount) throws SQLException {
        String sql = "select materials.id as id, materials.amount as materialsAmount, relation.amount as relationAmount " +
                "from products " +
                "inner join relation " +
                "on products.id = relation.pid " +
                "inner join materials " +
                "on relation.mid = materials.id " +
                "where products.id = ? and materials.amount >= relation.amount * ? " +
                "group by products.name, materials.name";
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, id);
        statement.setInt(2, amount);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int newAmount = resultSet.getInt("materialsAmount") - resultSet.getInt("relationAmount") * amount;
            sql = "update materials set materials.amount = ? where materials.id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, newAmount);
            statement.setInt(2, resultSet.getInt("id"));
            statement.executeUpdate();
        }
    }


    /**
     * Добавляет материал
     * @param name - наименование
     * @param amount - количество
     * @param price - цена
     */
    public void addStockMaterial(String name, int amount, long price) throws SQLException {
        String sql = "select * from materials where name = ?";
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()) {
            // Обновляем
            resultSet.updateInt("amount", amount + resultSet.getInt("amount"));
            resultSet.updateLong("price", price);
            resultSet.updateRow();
        } else {
            // Добавляем запись
            sql = "insert into materials(name,amount,price) values(?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, amount);
            statement.setLong(3, price);
            statement.executeUpdate();
        }
    }


    /**
     * Добавляет продукт
     * @param productName - наименование продукта
     * @param materialName - наименование материала
     * @param materialAmount - количество материала
     * @param materialPrice - цена материала
     */
    public void addStockProduct(String productName, String materialName, int materialAmount, long materialPrice) throws SQLException {
        // Добавляем материал
        addStockMaterial(materialName, materialAmount, materialPrice);
        int materialId = getMaterialId(materialName);
        // Добавляем продукт
        int productId = getProductId(productName);
        if(0 == productId) {
            String sql = "insert into products(name) values(?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, productName);
            statement.executeUpdate();
            productId = getProductId(productName);
            sql = "insert into relation values(?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.setInt(2, materialId);
            statement.setInt(3, materialAmount);
            statement.executeUpdate();
        } else {
            // редактируем зависимость
            String sql = "select * from relation where pid = ? and mid = ?";
            PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setInt(1, productId);
            statement.setInt(2, materialId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) sql = "update relation set amount = ? where pid = ? and mid = ?";
            else sql = "insert into relation(amount, pid, mid) values(?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, materialAmount);
            statement.setInt(2, productId);
            statement.setInt(3, materialId);
            statement.executeUpdate();
        }
    }
}
