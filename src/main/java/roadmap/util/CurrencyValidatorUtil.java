package roadmap.util;

import roadmap.exception.ValidationException;
import roadmap.model.dto.request.CurrencyRequestDto;

public final class CurrencyValidatorUtil {
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final String CODE_PATTERN = "[a-zA-Z]{3}";
    private static final int SIGN_LENGTH = 1;
    private static final String MISSING_NAME_MESSAGE = "Currency name is required.";
    private static final String MISSING_CODE_MESSAGE = "Currency code is required.";
    private static final String MISSING_SIGN_MESSAGE = "Currency sign is required.";
    private static final String INVALID_CODE_FORMAT_MESSAGE = "Currency code must be exactly 3 english letters.";
    private static final String INVALID_SIGN_LENGTH_MESSAGE = "Currency sign must be exactly 1 character.";
    private static final String INVALID_NAME_LENGTH_MESSAGE =
            "Currency name must be between %d and %d letters.".formatted(
                    MIN_NAME_LENGTH, MAX_NAME_LENGTH
            );

    private CurrencyValidatorUtil() {
    }

    public static void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException(MISSING_CODE_MESSAGE);
        }
        if (!code.matches(CODE_PATTERN)) {
            throw new ValidationException(INVALID_CODE_FORMAT_MESSAGE);
        }
    }

    public static void validateSign(String sign) {
        if (sign == null || sign.trim().isEmpty()) {
            throw new ValidationException(MISSING_SIGN_MESSAGE);
        }
        if (sign.length() != SIGN_LENGTH) {
            throw new ValidationException(INVALID_SIGN_LENGTH_MESSAGE);
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(MISSING_NAME_MESSAGE);
        }
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(INVALID_NAME_LENGTH_MESSAGE);
        }
    }
}