package model.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import model.MenuItem;
import model.payment.Payment;
import util.InputValidator;

public class Order {
    private final int orderId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private Payment payment;

    public Order(int orderId) {
        this(orderId, OrderStatus.PENDING, LocalDateTime.now(), new ArrayList<OrderItem>(), null);
    }

    private Order(int orderId, OrderStatus status, LocalDateTime createdAt, List<OrderItem> items, Payment payment) {
        InputValidator.requirePositive(orderId, "Order id");
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Order creation time cannot be null");
        }
        this.orderId = orderId;
        this.status = status;
        this.createdAt = createdAt;
        this.items = new ArrayList<>(items);
        this.payment = payment;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void addItem(MenuItem item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        InputValidator.requirePositive(quantity, "Quantity");
        Optional<OrderItem> existing = items.stream()
                .filter(orderItem -> orderItem.getItem().getId() == item.getId())
                .findFirst();

        // Composition: an order owns order rows, while each row points to a menu item.
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            items.add(new OrderItem(item, quantity));
        }
    }

    public void removeItem(int menuItemId) {
        boolean removed = items.removeIf(orderItem -> orderItem.getItem().getId() == menuItemId);
        if (!removed) {
            throw new IllegalArgumentException("Menu item is not in this order");
        }
    }

    public void updateQuantity(int menuItemId, int quantity) {
        InputValidator.requirePositive(quantity, "Quantity");
        OrderItem orderItem = items.stream()
                .filter(item -> item.getItem().getId() == menuItemId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Menu item is not in this order"));
        orderItem.setQuantity(quantity);
    }

    public double calculateTotal() {
        return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void setPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }

    public String toStorageString() {
        String itemsText = items.stream()
                .map(item -> item.getItem().getId() + ":" + item.getQuantity())
                .collect(Collectors.joining(","));
        String paymentText = payment == null ? "-" : payment.toStorageString();
        return String.join(";",
                "ORDER",
                String.valueOf(orderId),
                status.name(),
                createdAt.toString(),
                itemsText,
                paymentText);
    }

    public static Order fromStorageString(String line, List<MenuItem> menuItems) {
        String[] parts = line.split(";", -1);
        if ((parts.length != 5 && parts.length != 6) || !"ORDER".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid order storage line");
        }

        List<OrderItem> loadedItems = new ArrayList<>();
        if (!parts[4].trim().isEmpty()) {
            String[] itemTokens = parts[4].split(",");
            for (String itemToken : itemTokens) {
                String[] pair = itemToken.split(":");
                if (pair.length != 2) {
                    throw new IllegalArgumentException("Invalid order item storage token");
                }
                int itemId = Integer.parseInt(pair[0]);
                int quantity = Integer.parseInt(pair[1]);
                MenuItem menuItem = menuItems.stream()
                        .filter(item -> item.getId() == itemId)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Order references missing menu item"));
                loadedItems.add(new OrderItem(menuItem, quantity));
            }
        }

        Payment payment = parts.length == 6 ? Payment.fromStorageString(parts[5]) : null;
        return new Order(
                Integer.parseInt(parts[1]),
                OrderStatus.valueOf(parts[2]),
                LocalDateTime.parse(parts[3]),
                loadedItems,
                payment);
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + status + " - total " + String.format("%.0f AMD", calculateTotal());
    }
}
