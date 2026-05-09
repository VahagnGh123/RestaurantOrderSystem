package model.order;

import model.MenuItem;
import util.InputValidator;

public class OrderItem {
    private final MenuItem item;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        InputValidator.requirePositive(quantity, "Order item quantity");
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        InputValidator.requirePositive(quantity, "Order item quantity");
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }
}
