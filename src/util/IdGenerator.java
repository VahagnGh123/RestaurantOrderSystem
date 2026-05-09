package util;

import java.util.List;

import model.MenuItem;
import model.order.Order;
import model.payment.Payment;
import model.reservation.Reservation;

public class IdGenerator {
    private int nextMenuItemId = 1;
    private int nextOrderId = 1;
    private int nextReservationId = 1;
    private int nextPaymentId = 1;

    public int nextMenuItemId() {
        return nextMenuItemId++;
    }

    public int nextOrderId() {
        return nextOrderId++;
    }

    public int nextReservationId() {
        return nextReservationId++;
    }

    public int nextPaymentId() {
        return nextPaymentId++;
    }

    public void updateFromExistingData(List<MenuItem> menuItems, List<Order> orders, List<Reservation> reservations) {
        int maxMenuId = menuItems.stream().mapToInt(MenuItem::getId).max().orElse(0);
        int maxOrderId = orders.stream().mapToInt(Order::getOrderId).max().orElse(0);
        int maxReservationId = reservations.stream().mapToInt(Reservation::getReservationId).max().orElse(0);
        int maxPaymentId = orders.stream()
                .map(Order::getPayment)
                .filter(payment -> payment != null)
                .mapToInt(Payment::getPaymentId)
                .max()
                .orElse(0);

        nextMenuItemId = Math.max(nextMenuItemId, maxMenuId + 1);
        nextOrderId = Math.max(nextOrderId, maxOrderId + 1);
        nextReservationId = Math.max(nextReservationId, maxReservationId + 1);
        nextPaymentId = Math.max(nextPaymentId, maxPaymentId + 1);
    }
}
