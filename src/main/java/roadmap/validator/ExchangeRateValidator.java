package roadmap.validator;

import roadmap.exception.ValidationException;

import java.math.BigDecimal;

public final class ExchangeRateValidator {
    private static final String MISSING_RATE_ERROR = "Exchange rate is required.";
    private static final String MISSING_AMOUNT_ERROR = "Amount is required.";
    private static final BigDecimal MIN_RATE = BigDecimal.valueOf(0.00001);
    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final String INVALID_AMOUNT_MESSAGE =
            "Amount cannot be lesser than %s.".formatted(MIN_AMOUNT);
    public static final String INVALID_RATE_MESSAGE =
            "Exchange rate cannot be lesser than %s.".formatted(MIN_RATE);

    private ExchangeRateValidator() {
    }

    public static void validateRate(BigDecimal rate) {
        if (rate == null) {
            throw new ValidationException(MISSING_RATE_ERROR);
        }
        if (rate.compareTo(MIN_RATE) < 0) {
            throw new ValidationException(INVALID_RATE_MESSAGE);
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException(MISSING_AMOUNT_ERROR);
        }
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new ValidationException(INVALID_AMOUNT_MESSAGE);
        }
    }
}