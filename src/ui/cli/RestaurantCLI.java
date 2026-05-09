package ui.cli;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import model.MenuCategory;
import model.MenuItem;
import model.order.Order;
import model.order.OrderItem;
import model.order.OrderStatus;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.reservation.Reservation;
import model.reservation.Table;
import service.RestaurantManager;
import util.DateTimeUtil;

public class RestaurantCLI {
    private final RestaurantManager manager;
    private final Scanner scanner;

    public RestaurantCLI(RestaurantManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Restaurant manager cannot be null");
        }
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1:
                        handleViewMenu();
                        break;
                    case 2:
                        handleAddMenuItem();
                        break;
                    case 3:
                        handleApplyDiscount();
                        break;
                    case 4:
                        handleCreateOrder();
                        break;
                    case 5:
                        handleAddItemToOrder();
                        break;
                    case 6:
                        handleViewOrders();
                        break;
                    case 7:
                        handleUpdateOrderStatus();
                        break;
                    case 8:
                        handleAddTable();
                        break;
                    case 9:
                        handleViewTables();
                        break;
                    case 10:
                        handleCreateReservation();
                        break;
                    case 11:
                        handleViewReservations();
                        break;
                    case 12:
                        handleConfirmReservation();
                        break;
                    case 13:
                        handleCancelReservation();
                        break;
                    case 14:
                        handleProcessPayment();
                        break;
                    case 15:
                        handleSaveData();
                        break;
                    case 16:
                        handleLoadData();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (RuntimeException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
    }

    public void showMainMenu() {
        System.out.println();
        System.out.println("1. View menu");
        System.out.println("2. Add menu item");
        System.out.println("3. Apply discount to menu item");
        System.out.println("4. Create order");
        System.out.println("5. Add item to order");
        System.out.println("6. View orders");
        System.out.println("7. Update order status");
        System.out.println("8. Add table");
        System.out.println("9. View tables");
        System.out.println("10. Create reservation");
        System.out.println("11. View reservations");
        System.out.println("12. Confirm reservation");
        System.out.println("13. Cancel reservation");
        System.out.println("14. Process payment");
        System.out.println("15. Save data");
        System.out.println("16. Load data");
        System.out.println("0. Exit");
    }

    public void handleViewMenu() {
        if (manager.getAllMenuItems().isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }
        for (MenuItem item : manager.getAllMenuItems()) {
            System.out.println(item.getId() + ". " + item);
        }
    }

    public void handleAddMenuItem() {
        String name = readString("Name: ");
        double price = readDouble("Price: ");
        MenuCategory category = readEnum("Category (FOOD, DRINK, DESSERT, OTHER): ", MenuCategory.class);
        boolean available = readBoolean("Available (true/false): ");
        int calories = readInt("Calories (0 if not used): ");
        double volume = readDouble("Volume in liters (0 if not used): ");
        int sugarLevel = readInt("Sugar level 0-10 (0 if not used): ");
        MenuItem item = manager.createMenuItem(name, price, category, available, calories, volume, sugarLevel);
        System.out.println("Added menu item #" + item.getId());
    }

    public void handleApplyDiscount() {
        int itemId = readInt("Menu item id: ");
        double percent = readDouble("Discount percent: ");
        manager.applyDiscountToMenuItem(itemId, percent);
        System.out.println("Discount applied.");
    }

    public void handleCreateOrder() {
        Order order = manager.createOrder();
        System.out.println("Created order #" + order.getOrderId());
    }

    public void handleAddItemToOrder() {
        int orderId = readInt("Order id: ");
        int menuItemId = readInt("Menu item id: ");
        int quantity = readInt("Quantity: ");
        manager.addItemToOrder(orderId, menuItemId, quantity);
        System.out.println("Item added.");
    }

    public void handleViewOrders() {
        if (manager.getAllOrders().isEmpty()) {
            System.out.println("No orders.");
            return;
        }
        for (Order order : manager.getAllOrders()) {
            System.out.println(order);
            for (OrderItem item : order.getItems()) {
                System.out.println("  " + item.getItem().getName() + " x" + item.getQuantity()
                        + " = " + String.format("%.0f AMD", item.getTotalPrice()));
            }
            if (order.getPayment() != null) {
                System.out.println("  Payment: " + order.getPayment().getStatus());
            }
        }
    }

    public void handleUpdateOrderStatus() {
        int orderId = readInt("Order id: ");
        OrderStatus status = readEnum("Status (PENDING, PREPARING, READY, SERVED, CANCELLED): ", OrderStatus.class);
        manager.updateOrderStatus(orderId, status);
        System.out.println("Order status updated.");
    }

    public void handleAddTable() {
        int tableNumber = readInt("Table number: ");
        int capacity = readInt("Capacity: ");
        manager.addTable(new Table(tableNumber, capacity));
        System.out.println("Table added.");
    }

    public void handleViewTables() {
        if (manager.getAllTables().isEmpty()) {
            System.out.println("No tables.");
            return;
        }
        for (Table table : manager.getAllTables()) {
            System.out.println(table);
        }
    }

    public void handleCreateReservation() {
        String customerName = readString("Customer name: ");
        int tableNumber = readInt("Table number: ");
        LocalDateTime start = DateTimeUtil.parseDateTime(readString("Start (yyyy-MM-ddTHH:mm): "));
        Duration duration = DateTimeUtil.parseDurationHours(readInt("Duration hours: "));
        Reservation reservation = manager.createReservation(customerName, tableNumber, start, duration);
        System.out.println("Created reservation #" + reservation.getReservationId());
    }

    public void handleViewReservations() {
        if (manager.getAllReservations().isEmpty()) {
            System.out.println("No reservations.");
            return;
        }
        for (Reservation reservation : manager.getAllReservations()) {
            System.out.println(reservation);
        }
    }

    public void handleConfirmReservation() {
        manager.confirmReservation(readInt("Reservation id: "));
        System.out.println("Reservation confirmed.");
    }

    public void handleCancelReservation() {
        manager.cancelReservation(readInt("Reservation id: "));
        System.out.println("Reservation cancelled.");
    }

    public void handleProcessPayment() {
        int orderId = readInt("Order id: ");
        String type = readString("Payment type (cash/card): ").trim().toLowerCase();
        if ("cash".equals(type)) {
            CashPayment payment = manager.processCashPayment(orderId, readDouble("Cash received: "));
            System.out.println("Payment status: " + payment.getStatus());
            System.out.println("Change: " + String.format("%.0f AMD", payment.calculateChange()));
        } else if ("card".equals(type)) {
            CardPayment payment = manager.processCardPayment(orderId, readString("Card number: "));
            System.out.println("Payment status: " + payment.getStatus());
            System.out.println("Card: " + payment.getMaskedCardNumber());
        } else {
            System.out.println("Unknown payment type.");
        }
    }

    public void handleSaveData() {
        manager.saveData();
        System.out.println("Data saved.");
    }

    public void handleLoadData() {
        manager.loadData();
        System.out.println("Data loaded.");
    }

    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            if (!value.trim().isEmpty()) {
                return value;
            }
            System.out.println("Value cannot be empty.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim().toLowerCase();
            if ("true".equals(value) || "yes".equals(value) || "y".equals(value)) {
                return true;
            }
            if ("false".equals(value) || "no".equals(value) || "n".equals(value)) {
                return false;
            }
            System.out.println("Enter true or false.");
        }
    }

    private <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        while (true) {
            System.out.print(prompt);
            try {
                return Enum.valueOf(enumClass, scanner.nextLine().trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                System.out.println("Enter one of the listed values.");
            }
        }
    }
}
