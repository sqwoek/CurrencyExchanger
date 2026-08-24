package roadmap.util;

import roadmap.exception.ValidationException;
import roadmap.model.entity.CurrencyCodePair;

import java.math.BigDecimal;

public final class ExchangeRateValidatorUtil {
    private static final String MISSING_AMOUNT_ERROR = "Amount is required.";
    private static final String MISSING_CODE_PAIR_ERROR = "Code pair is required.";
    private static final BigDecimal MIN_RATE = BigDecimal.valueOf(0.00001);
    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final String INVALID_AMOUNT_MESSAGE =
            "Amount cannot be lesser than %s.".formatted(MIN_AMOUNT);
    public static final String MISSING_RATE_ERROR = "Exchange rate is required.";
    public static final String INVALID_RATE_MESSAGE =
            "Exchange rate cannot be lesser than %s.".formatted(MIN_RATE);
    public static final String INVALID_STRING_RATE =
            "Exchange rate must be a number and cannot be lesser than %s.".formatted(MIN_RATE);
    public static final String INVALID_STRING_AMOUNT =
            "Exchange amount must be a number and cannot be lesser than %s.".formatted(MIN_RATE);

    private ExchangeRateValidatorUtil() {
    }

    public static void validateRate(String rate) {
        validateStringToBigDecimal(rate, MISSING_RATE_ERROR, MIN_RATE, INVALID_RATE_MESSAGE, INVALID_STRING_RATE);
    }

    public static void validateAmount(String amount) {
        validateStringToBigDecimal(amount, MISSING_AMOUNT_ERROR, MIN_AMOUNT, INVALID_AMOUNT_MESSAGE, INVALID_STRING_AMOUNT);
    }

    public static void validateCodePair(CurrencyCodePair codePair) {
        if (codePair == null) {
            throw new ValidationException(MISSING_CODE_PAIR_ERROR);
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