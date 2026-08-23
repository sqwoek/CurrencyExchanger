package roadmap.dao;

import roadmap.model.entity.ExchangeRateSaveEntity;
import roadmap.util.ConnectionManager;
import roadmap.model.CurrencyCodePair;
import roadmap.model.ExchangeRateResponse;
import roadmap.model.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDao {
    private static final String SAVE_WITH_CODES_QUERY = """
            INSERT INTO exchangeRates (base_currency_id, target_currency_id, rate)
            VALUES ((SELECT id FROM currencies WHERE code = ?),
                    (SELECT id FROM currencies WHERE code = ?),
                    ?)
            """;
    private static final String GET_BY_CODES_QUERY = """
            SELECT exchangeRates.*
            FROM exchangeRates
            JOIN currencies base ON base_currency_id = base.id
            JOIN currencies target ON target_currency_id = target.id
            WHERE base.code = ? AND target.code = ?
            """;
    private static final String FIND_ALL_QUERY = "SELECT * FROM exchangeRates";
    private static final String UPDATE_QUERY = "UPDATE exchangeRates SET rate = ? WHERE id = ?";
    private static final String FIND_BY_ID = """
            SELECT id, base_currency_id, target_currency_id, rate FROM exchangeRates WHERE id = ?
            """;

    public void save(ExchangeRateSaveEntity exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_WITH_CODES_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, exchangeRate.baseCurrencyCode());
            statement.setString(2, exchangeRate.targetCurrencyCode());
            statement.setBigDecimal(3, exchangeRate.rate());
            statement.executeUpdate();
        } catch (
                SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.save()" + ex.getMessage());
        }
    }

    public ExchangeRateEntity getByCode(CurrencyCodePair codePair) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODES_QUERY)) {
            statement.setString(1, codePair.baseCurrencyCode());
            statement.setString(2, codePair.targetCurrencyCode());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Long id = result.getLong("id");
                    Long baseCurrencyId = result.getLong("base_currency_id");
                    Long targetCurrencyId = result.getLong("target_currency_id");
                    BigDecimal rate = result.getBigDecimal("rate");
                    return new ExchangeRateEntity(id, baseCurrencyId, targetCurrencyId, rate);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.getByCode()" + ex.getMessage());
        }
        return null;
    }

    public List<ExchangeRateEntity> findAll() {
        List<ExchangeRateEntity> exchangeRates = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Long id = result.getLong("id");
                Long baseCurrencyId = result.getLong("base_currency_id");
                Long targetCurrencyId = result.getLong("target_currency_id");
                BigDecimal rate = result.getBigDecimal("rate");
                ExchangeRateEntity exchangeRate = new ExchangeRateEntity(id, baseCurrencyId, targetCurrencyId, rate);
                exchangeRates.add(exchangeRate);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.findAll()" + ex.getMessage());
        }
        return exchangeRates;
    }

    public ExchangeRateEntity update(ExchangeRateResponse exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setBigDecimal(1, exchangeRate.rate());
            statement.setLong(2, exchangeRate.id());
            statement.executeUpdate();

            return findById(exchangeRate.id());
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in updateRate(): " + ex.getMessage(), ex);
        }
    }

    private ExchangeRateEntity findById(Long id) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new ExchangeRateEntity(
                        result.getLong("id"),
                        result.getLong("base_currency_id"),
                        result.getLong("target_currency_id"),
                        result.getBigDecimal("rate")
                );
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error finding exchange rate: " + ex.getMessage(), ex);
        }
        return null;
    }
}
