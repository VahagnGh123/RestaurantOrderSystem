package model.payment;

public class CashPayment extends Payment {
    private final double cashReceived;

    public CashPayment(int paymentId, double amount, double cashReceived) {
        super(paymentId, amount);
        if (cashReceived < 0) {
            throw new IllegalArgumentException("Cash received cannot be negative");
        }
        this.cashReceived = cashReceived;
    }

    @Override
    public boolean processPayment() {
        if (cashReceived >= getAmount()) {
            markSuccessful();
            return true;
        }
        markFailed();
        return false;
    }

    public double calculateChange() {
        return cashReceived - getAmount();
    }

    public double getCashReceived() {
        return cashReceived;
    }

    @Override
    public String toStorageString() {
        String paidAtText = getPaidAt() == null ? "-" : getPaidAt().toString();
        return "CASH|" + getPaymentId() + "|" + getAmount() + "|" + paidAtText + "|"
                + getStatus().name() + "|" + cashReceived;
    }
}
