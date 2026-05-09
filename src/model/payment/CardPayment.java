package model.payment;

import util.InputValidator;

public class CardPayment extends Payment {
    private final String maskedCardNumber;
    private final boolean validForProcessing;

    public CardPayment(int paymentId, double amount, String cardNumber) {
        this(paymentId, amount, maskCardNumber(cardNumber), InputValidator.isValidCardNumber(cardNumber));
    }

    private CardPayment(int paymentId, double amount, String maskedCardNumber, boolean validForProcessing) {
        super(paymentId, amount);
        this.maskedCardNumber = maskedCardNumber == null || maskedCardNumber.trim().isEmpty()
                ? "****"
                : maskedCardNumber.trim();
        this.validForProcessing = validForProcessing;
    }

    public static CardPayment fromStoredMaskedCard(int paymentId, double amount, String maskedCardNumber) {
        return new CardPayment(paymentId, amount, maskedCardNumber, false);
    }

    @Override
    public boolean processPayment() {
        if (validForProcessing) {
            markSuccessful();
            return true;
        }
        markFailed();
        return false;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    @Override
    public String toStorageString() {
        String paidAtText = getPaidAt() == null ? "-" : getPaidAt().toString();
        // Persist only the masked card value; the full card number is never saved or displayed.
        return "CARD|" + getPaymentId() + "|" + getAmount() + "|" + paidAtText + "|"
                + getStatus().name() + "|" + maskedCardNumber;
    }

    private static String maskCardNumber(String cardNumber) {
        String normalized = cardNumber == null ? "" : cardNumber.trim();
        if (normalized.length() < 4) {
            return "****";
        }
        String lastFour = normalized.substring(normalized.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
