package roadmap.dao;

import roadmap.ConnectionManager;
import roadmap.model.CurrencyEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {
    private final ConnectionManager connectionManager;
    private static final String SAVE_QUERY = "INSERT INTO currencies(code, full_name, sign) values (?, ?, ?)";
    private static final String GET_QUERY = "SELECT * FROM currencies WHERE code = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM currencies";

    public CurrencyDao(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void save(CurrencyEntity currency) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_QUERY)) {
            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getName());
            statement.setString(3, currency.getSign());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.save()" + ex.getMessage());
        }
    }

    public CurrencyEntity getByCode(String code) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_QUERY)) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    long id = result.getLong("id");
                    String name = result.getString("full_name");
                    String sign = result.getString("sign");
                    return new CurrencyEntity(id, code, name, sign);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.getByCode()" + ex.getMessage());
        }
        return null;
    }

    public List<CurrencyEntity> findAll() {
        List<CurrencyEntity> currencies = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                long id = result.getLong("id");
                String code = result.getString("code");
                String name = result.getString("full_name");
                String sign = result.getString("sign");
                CurrencyEntity currency = new CurrencyEntity(id, code, name, sign);
                currencies.add(currency);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.findAll()" + ex.getMessage());
        }
        return currencies;
    }
}