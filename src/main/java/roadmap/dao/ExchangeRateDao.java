package roadmap.dao;

import roadmap.model.entity.CurrencyEntity;
import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.util.ConnectionManagerUtil;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateEntity;

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
    private static final String FIND_ALL_QUERY = """
            SELECT
                exchange.id AS exchange_id, exchange.rate AS exchange_rate,
                base.id AS base_id, base.code AS base_code, base.full_name AS base_name, base.sign AS base_sign,
                target.id AS target_id, target.code AS target_code, target.full_name AS target_name, target.sign AS target_sign
            FROM exchangeRates exchange
            JOIN currencies base ON base_currency_id = base.id
            JOIN currencies target ON target_currency_id = target.id
            """;
    private static final String UPDATE_BY_CODES_QUERY = """
            UPDATE exchangeRates
            SET rate = ?
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
             AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;
    private static final String FIND_BY_CURRENCY_CODES = FIND_ALL_QUERY + "WHERE base.code = ? AND target.code = ?";

    public void save(ExchangeRateUpdateEntity exchangeRate) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_WITH_CODES_QUERY)) {
            statement.setString(1, exchangeRate.baseCurrencyCode());
            statement.setString(2, exchangeRate.targetCurrencyCode());
            statement.setBigDecimal(3, exchangeRate.rate());
            statement.executeUpdate();
        } catch (
                SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.save()" + ex.getMessage());
        }
    }

    public ExchangeRateEntity findByCodes(CurrencyCodePair codePair) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CURRENCY_CODES)) {
            statement.setString(1, codePair.baseCurrencyCode());
            statement.setString(2, codePair.targetCurrencyCode());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    CurrencyEntity baseCurrencyEntity = new CurrencyEntity(
                            result.getLong("base_id"),
                            result.getString("base_name"),
                            result.getString("base_code"),
                            result.getString("base_sign")
                    );
                    CurrencyEntity targetCurrencyEntity = new CurrencyEntity(
                            result.getLong("target_id"),
                            result.getString("target_name"),
                            result.getString("target_code"),
                            result.getString("target_sign")
                    );
                    return new ExchangeRateEntity(
                            result.getLong("exchange_id"),
                            baseCurrencyEntity,
                            targetCurrencyEntity,
                            result.getBigDecimal("exchange_rate")
                    );
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.getByCode()" + ex.getMessage());
        }
        return null;
    }

    public List<ExchangeRateEntity> findAll() {
        List<ExchangeRateEntity> exchangeRates = new ArrayList<>();
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                CurrencyEntity baseCurrencyEntity = new CurrencyEntity(
                        result.getLong("base_id"),
                        result.getString("base_name"),
                        result.getString("base_code"),
                        result.getString("base_sign")
                );
                CurrencyEntity targetCurrencyEntity = new CurrencyEntity(
                        result.getLong("target_id"),
                        result.getString("target_name"),
                        result.getString("target_code"),
                        result.getString("target_sign")
                );
                ExchangeRateEntity exchangeRate = new ExchangeRateEntity(
                        result.getLong("exchange_id"),
                        baseCurrencyEntity,
                        targetCurrencyEntity,
                        result.getBigDecimal("exchange_rate")
                );
                exchangeRates.add(exchangeRate);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in ExchangeRateDao.findAll()" + ex.getMessage());
        }
        return exchangeRates;
    }

    public void update(ExchangeRateUpdateEntity exchangeRate) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BY_CODES_QUERY)) {
            statement.setBigDecimal(1, exchangeRate.rate());
            statement.setString(2, exchangeRate.baseCurrencyCode());
            statement.setString(3, exchangeRate.targetCurrencyCode());

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Exception in updateRate(): " + ex.getMessage(), ex);
        }
    }
}
