package util;

public final class InputValidator {
    private InputValidator() {
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    public static void requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    public static void requireDiscountPercent(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
    }

    public static void requireSugarLevel(int sugarLevel) {
        if (sugarLevel < 0 || sugarLevel > 10) {
            throw new IllegalArgumentException("Sugar level must be between 0 and 10");
        }
    }

    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{12,19}");
    }
}
