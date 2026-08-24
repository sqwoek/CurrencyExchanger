package roadmap.dao;

import roadmap.exception.DatabaseException;
import roadmap.exception.EntityAlreadyExists;
import roadmap.model.entity.CurrencyEntity;
import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.util.ConnectionManagerUtil;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    private static final int CONSTRAINT_UNIQUE_ERROR = 19;
    private static final String CURRENCY_EXISTS_QUERY = "SELECT 1 FROM currencies WHERE code = ?";

    public void save(ExchangeRateUpdateEntity exchangeRate) {
        int rowsInserted = 0;
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_WITH_CODES_QUERY)) {
            statement.setString(1, exchangeRate.baseCurrencyCode());
            statement.setString(2, exchangeRate.targetCurrencyCode());
            statement.setBigDecimal(3, exchangeRate.rate());

            rowsInserted = statement.executeUpdate();
        } catch (SQLException ex) {
            if (rowsInserted == 0) {
                checkCurrencyExists(exchangeRate.baseCurrencyCode());
                checkCurrencyExists(exchangeRate.targetCurrencyCode());
            }
            if (ex.getErrorCode() == CONSTRAINT_UNIQUE_ERROR) {
                throw new EntityAlreadyExists("Exchange rate with code pair %s, %s already exists.".formatted(
                        exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode()));
            } else {
                throw new DatabaseException();
            }
        }
    }

    public ExchangeRateEntity findByCodes(CurrencyCodePair codePair) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CURRENCY_CODES)) {
            statement.setString(1, codePair.baseCurrencyCode());
            statement.setString(2, codePair.targetCurrencyCode());

            ResultSet result = statement.executeQuery();
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
            } else {
                checkCurrencyExists(codePair.baseCurrencyCode());
                checkCurrencyExists(codePair.targetCurrencyCode());
            }
        } catch (SQLException ex) {
            throw new DatabaseException();
        }
        throw new NoSuchElementException("Exchange rate with code pair %s, %s not found.".formatted(
                codePair.baseCurrencyCode(), codePair.targetCurrencyCode()
        ));
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
            throw new DatabaseException();
        }
        return exchangeRates;
    }

    public void update(ExchangeRateUpdateEntity exchangeRate) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BY_CODES_QUERY)) {
            statement.setBigDecimal(1, exchangeRate.rate());
            statement.setString(2, exchangeRate.baseCurrencyCode());
            statement.setString(3, exchangeRate.targetCurrencyCode());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new NoSuchElementException("Exchange rate with pair code %s, %s not found.".formatted(
                        exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode())
                );
            }
        } catch (SQLException ex) {
            throw new DatabaseException();
        }
    }

    private void checkCurrencyExists(String code) {
        try (Connection connection = ConnectionManagerUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(CURRENCY_EXISTS_QUERY)) {
            statement.setString(1, code);
            ResultSet result = statement.executeQuery();
            if (!result.next()) {
                throw new NoSuchElementException("Currency with code %s not found.".formatted(code));
            }
        } catch (SQLException ex) {
            throw new DatabaseException();
        }
    }
}
