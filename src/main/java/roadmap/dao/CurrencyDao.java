package roadmap.dao;

import roadmap.util.ConnectionManager;
import roadmap.model.entity.CurrencyEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {
    private static final String SAVE_QUERY = "INSERT INTO currencies(code, full_name, sign) values (?, ?, ?)";
    private static final String GET_BY_CODE_QUERY = "SELECT * FROM currencies WHERE code = ?";
    private static final String GET_BY_ID = "SELECT * FROM currencies WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM currencies";

    public CurrencyEntity save(CurrencyEntity currency) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getName());
            statement.setString(3, currency.getSign());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new CurrencyEntity(id, currency.getCode(), currency.getName(), currency.getSign());
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.save()" + ex.getMessage());
        }
        return null;
    }

    public CurrencyEntity getByCode(String code) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_QUERY)) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Long id = result.getLong("id");
                    String name = result.getString("full_name");
                    String sign = result.getString("sign");
                    return new CurrencyEntity(id, name, code, sign);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.getByCode()" + ex.getMessage());
        }
        return null;
    }

    public List<CurrencyEntity> findAll() {
        List<CurrencyEntity> currencies = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Long id = result.getLong("id");
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

    public Long getIdByCode(String code) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_QUERY)) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Long id = result.getLong("id");
                    return id;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.getIdByCode()" + ex.getMessage());
        }
        return null;
    }

    public CurrencyEntity get(Long id) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String name = result.getString("full_name");
                    String code = result.getString("code");
                    String sign = result.getString("sign");
                    return new CurrencyEntity(id, code, name, sign);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.get()" + ex.getMessage());
        }
        return null;
    }
}