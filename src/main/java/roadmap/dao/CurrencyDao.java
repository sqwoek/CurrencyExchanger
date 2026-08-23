package roadmap.dao;

import roadmap.util.ConnectionManagerUtil;
import roadmap.model.entity.CurrencyEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDao {
    private static final String SAVE_QUERY = "INSERT INTO currencies(code, full_name, sign) values (?, ?, ?)";
    private static final String GET_BY_CODE_QUERY = "SELECT * FROM currencies WHERE code = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM currencies";

    public CurrencyEntity save(CurrencyEntity currencyEntity) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, currencyEntity.code());
            statement.setString(2, currencyEntity.name());
            statement.setString(3, currencyEntity.sign());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new CurrencyEntity(id, currencyEntity.code(), currencyEntity.name(), currencyEntity.sign());
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.save()" + ex.getMessage());
        }
        return null;
    }

    public CurrencyEntity getByCode(String code) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
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
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Long id = result.getLong("id");
                String code = result.getString("code");
                String name = result.getString("full_name");
                String sign = result.getString("sign");
                CurrencyEntity currencyEntity = new CurrencyEntity(id, name, code, sign);
                currencies.add(currencyEntity);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in CurrencyDao.findAll()" + ex.getMessage());
        }
        return currencies;
    }
}