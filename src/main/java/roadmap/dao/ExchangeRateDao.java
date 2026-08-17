package roadmap.dao;

import roadmap.ConnectionManager;
import roadmap.model.CodePair;
import roadmap.model.ExchangeRateResponse;
import roadmap.model.entity.ExchangeRateEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDao {
    private final static String SAVE_QUERY = """
            INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate) values (?, ?, ?)
            """;
    private static final String GET_BY_CODE_QUERY = """
            SELECT * FROM exchangeRates WHERE base_currency_id = ? AND target_currency_id = ?
            """;
    private static final String FIND_ALL_QUERY = "SELECT * FROM exchangeRates";
    private static final String UPDATE_QUERY = "UPDATE exchangeRates SET rate = ? WHERE id = ?";
    private static final String FIND_BY_ID = """
            SELECT id, base_currency_id, target_currency_id, rate FROM exchangeRates WHERE id = ?
            """;
    private final ConnectionManager connectionManager;

    public ExchangeRateDao(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ExchangeRateEntity save(ExchangeRateEntity exchangeRate) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, exchangeRate.getBaseCurrencyId());
            statement.setLong(2, exchangeRate.getTargetCurrencyId());
            statement.setDouble(3, exchangeRate.getRate());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new ExchangeRateEntity(
                            id,
                            exchangeRate.getBaseCurrencyId(),
                            exchangeRate.getTargetCurrencyId(),
                            exchangeRate.getRate()
                    );
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.save()" + ex.getMessage());
        }
        return null;
    }

    public ExchangeRateEntity getByCode(CodePair codePair) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_QUERY)) {
            statement.setLong(1, codePair.baseCurrencyId());
            statement.setLong(2, codePair.targetCurrencyId());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Long id = result.getLong("id");
                    Double rate = result.getDouble("rate");
                    return new ExchangeRateEntity(id, codePair.baseCurrencyId(), codePair.targetCurrencyId(), rate);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.getByCode()" + ex.getMessage());
        }
        return null;
    }

    public List<ExchangeRateEntity> findAll() {
        List<ExchangeRateEntity> exchangeRates = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Long id = result.getLong("id");
                Long baseCurrencyId = result.getLong("base_currency_id");
                Long targetCurrencyId = result.getLong("target_currency_id");
                Double rate = result.getDouble("rate");
                ExchangeRateEntity exchangeRate = new ExchangeRateEntity(id, baseCurrencyId, targetCurrencyId, rate);
                exchangeRates.add(exchangeRate);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.findAll()" + ex.getMessage());
        }
        return exchangeRates;
    }

    public ExchangeRateEntity update(ExchangeRateResponse exchangeRate) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setDouble(1, exchangeRate.rate());
            statement.setLong(2, exchangeRate.id());
            statement.executeUpdate();

            return findById(exchangeRate.id());
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in updateRate(): " + ex.getMessage(), ex);
        }
    }

    private ExchangeRateEntity findById(Long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new ExchangeRateEntity(
                        result.getLong("id"),
                        result.getLong("base_currency_id"),
                        result.getLong("target_currency_id"),
                        result.getDouble("rate")
                );
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error finding exchange rate: " + ex.getMessage(), ex);
        }
        return null;
    }
}
