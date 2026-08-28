package roadmap.util;

import roadmap.exception.ValidationException;
import roadmap.model.entity.CurrencyCodePair;

import java.math.BigDecimal;

public final class ExchangeRateValidatorUtil {
    private static final String MISSING_AMOUNT_MESSAGE = "Amount is required.";
    private static final String MISSING_CODE_PAIR_MESSAGE = "Code pair is required.";
    private static final BigDecimal MIN_RATE = BigDecimal.valueOf(0.00001).stripTrailingZeros();
    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final String INVALID_AMOUNT_MESSAGE =
            "Amount cannot be lesser than %s.".formatted(MIN_AMOUNT);
    public static final String MISSING_RATE_MESSAGE = "Exchange rate is required.";
    public static final String INVALID_RATE_MESSAGE =
            "Exchange rate cannot be lesser than %s.".formatted(MIN_RATE);
    public static final String INVALID_STRING_RATE_MESSAGE =
            "Exchange rate must be a number and cannot be lesser than %s.".formatted(MIN_RATE);
    public static final String INVALID_STRING_AMOUNT_MESSAGE =
            "Exchange amount must be a number and cannot be lesser than %s.".formatted(MIN_RATE);
    private static final String SAME_CURRENCY_MESSAGE = "Cannot exchange currency to itself.";

    private ExchangeRateValidatorUtil() {
    }

    public static void validateRate(String rate) {
        validateStringToBigDecimal(rate, MISSING_RATE_MESSAGE, MIN_RATE, INVALID_RATE_MESSAGE, INVALID_STRING_RATE_MESSAGE);
    }

    public static void validateAmount(String amount) {
        validateStringToBigDecimal(amount, MISSING_AMOUNT_MESSAGE, MIN_AMOUNT, INVALID_AMOUNT_MESSAGE, INVALID_STRING_AMOUNT_MESSAGE);
    }

    public static void validateCodePair(CurrencyCodePair codePair) {
        if (codePair == null) {
            throw new ValidationException(MISSING_CODE_PAIR_MESSAGE);
        }
        if (codePair.baseCurrencyCode().equals(codePair.targetCurrencyCode())) {
            throw new ValidationException(SAME_CURRENCY_MESSAGE);
        }
        CurrencyValidatorUtil.validateCode(codePair.baseCurrencyCode());
        CurrencyValidatorUtil.validateCode(codePair.targetCurrencyCode());
    }

    private static void validateStringToBigDecimal(
            String parameter,
            String missingParameterError,
            BigDecimal minParameter,
            String invalidParameterMessage,
            String invalidStringParameter
    ) {
        if (parameter == null) {
            throw new ValidationException(missingParameterError);
        }
        try {
            BigDecimal bigDecimalRate = new BigDecimal(parameter);

            if (bigDecimalRate.compareTo(minParameter) < 0) {
                throw new ValidationException(invalidParameterMessage);
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException(invalidStringParameter);
        }
    }
}