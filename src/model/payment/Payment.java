package model.payment;

import java.time.LocalDateTime;

import util.InputValidator;

public abstract class Payment {
    private final int paymentId;
    private final double amount;
    private LocalDateTime paidAt;
    private PaymentStatus status;

    protected Payment(int paymentId, double amount) {
        InputValidator.requirePositive(paymentId, "Payment id");
        InputValidator.requirePositive(amount, "Payment amount");
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    protected void markSuccessful() {
        status = PaymentStatus.SUCCESSFUL;
        paidAt = LocalDateTime.now();
    }

    protected void markFailed() {
        status = PaymentStatus.FAILED;
        paidAt = LocalDateTime.now();
    }

    protected void restoreStatus(PaymentStatus status, LocalDateTime paidAt) {
        if (status == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }
        this.status = status;
        this.paidAt = paidAt;
    }

    // Subclasses decide how a payment succeeds, while callers can use the shared Payment type.
    public abstract boolean processPayment();

    public abstract String toStorageString();

    public static Payment fromStorageString(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value)) {
            return null;
        }
        String[] parts = value.split("\\|", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid payment storage value");
        }

        String type = parts[0];
        int paymentId = Integer.parseInt(parts[1]);
        double amount = Double.parseDouble(parts[2]);
        LocalDateTime paidAt = "-".equals(parts[3]) ? null : LocalDateTime.parse(parts[3]);
        PaymentStatus status = PaymentStatus.valueOf(parts[4]);

        Payment payment;
        if ("CASH".equals(type)) {
            payment = new CashPayment(paymentId, amount, Double.parseDouble(parts[5]));
        } else if ("CARD".equals(type)) {
            payment = CardPayment.fromStoredMaskedCard(paymentId, amount, parts[5]);
        } else {
            throw new IllegalArgumentException("Unknown payment type");
        }

        payment.restoreStatus(status, paidAt);
        return payment;
    }
}
