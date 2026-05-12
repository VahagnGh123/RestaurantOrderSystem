package service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.MenuCategory;
import model.MenuItem;
import model.order.Order;
import model.order.OrderItem;
import model.order.OrderStatus;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.payment.PaymentStatus;
import model.reservation.Reservation;
import model.reservation.ReservationStatus;
import model.reservation.Table;
import repository.RestaurantRepository;
import util.IdGenerator;

public class RestaurantManager {
    private List<MenuItem> menuItems;
    private List<Order> orders;
    private List<Table> tables;
    private List<Reservation> reservations;
    private final RestaurantRepository repository;
    private final IdGenerator idGenerator;

    public RestaurantManager(RestaurantRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        this.repository = repository;
        this.idGenerator = new IdGenerator();
        this.menuItems = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    public MenuItem createMenuItem(String name, double price, MenuCategory category, boolean available,
                                   int calories, double volume, int sugarLevel) {
        MenuItem item = new MenuItem(idGenerator.nextMenuItemId(), name, price, category, available,
                calories, volume, sugarLevel);
        addMenuItem(item);
        return item;
    }

    public void addMenuItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        if (findMenuItemById(item.getId()) != null) {
            throw new IllegalArgumentException("Duplicate menu item id");
        }
        menuItems.add(item);
    }

    public void removeMenuItemById(int id) {
        if (isMenuItemUsedInOrders(id)) {
            throw new IllegalArgumentException("Cannot delete menu item because it is used in an order");
        }

        boolean removed = false;
        for (int i = menuItems.size() - 1; i >= 0; i--) {
            if (menuItems.get(i).getId() == id) {
                menuItems.remove(i);
                removed = true;
            }
        }

        if (!removed) {
            throw new IllegalArgumentException("Menu item not found");
        }
    }

    public MenuItem findMenuItemById(int id) {
        for (MenuItem item : menuItems) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public List<MenuItem> getAllMenuItems() {
        return new ArrayList<>(menuItems);
    }

    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> availableItems = new ArrayList<>();
        for (MenuItem item : menuItems) {
            if (item.isAvailable()) {
                availableItems.add(item);
            }
        }
        return availableItems;
    }

    public void applyDiscountToMenuItem(int itemId, double percent) {
        MenuItem item = requireMenuItem(itemId);
        item.applyDiscount(percent);
    }

    public void updateMenuItem(int itemId, String name, double price, MenuCategory category, boolean available,
                               int calories, double volume, int sugarLevel) {
        MenuItem item = requireMenuItem(itemId);
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setAvailable(available);
        item.setCalories(calories);
        item.setVolume(volume);
        item.setSugarLevel(sugarLevel);
    }

    public void setMenuItemAvailability(int itemId, boolean available) {
        requireMenuItem(itemId).setAvailable(available);
    }

    private boolean isMenuItemUsedInOrders(int menuItemId) {
        for (Order order : orders) {
            for (OrderItem orderItem : order.getItems()) {
                if (orderItem.getItem().getId() == menuItemId) {
                    return true;
                }
            }
        }
        return false;
    }

    public Order createOrder() {
        Order order = new Order(idGenerator.nextOrderId());
        orders.add(order);
        return order;
    }

    public Order findOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public long countOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        long count = 0;
        for (Order order : orders) {
            if (order.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public void addItemToOrder(int orderId, int menuItemId, int quantity) {
        Order order = requireOrder(orderId);
        MenuItem item = requireMenuItem(menuItemId);
        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Cannot add unavailable menu item to order");
        }
        order.addItem(item, quantity);
    }

    public void removeItemFromOrder(int orderId, int menuItemId) {
        requireOrder(orderId).removeItem(menuItemId);
    }

    public void updateOrderItemQuantity(int orderId, int menuItemId, int quantity) {
        requireOrder(orderId).updateQuantity(menuItemId, quantity);
    }

    public double calculateOrderTotal(int orderId) {
        return requireOrder(orderId).calculateTotal();
    }

    public void updateOrderStatus(int orderId, OrderStatus status) {
        requireOrder(orderId).setStatus(status);
    }

    public void addTable(Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (findTableByNumber(table.getTableNumber()) != null) {
            throw new IllegalArgumentException("Duplicate table number");
        }
        tables.add(table);
    }

    public Table createTable(int tableNumber, int capacity) {
        Table table = new Table(tableNumber, capacity);
        addTable(table);
        return table;
    }

    public void removeTableByNumber(int tableNumber) {
        if (isTableUsedInReservations(tableNumber)) {
            throw new IllegalArgumentException("Cannot delete table because it is used in a reservation");
        }

        boolean removed = false;
        for (int i = tables.size() - 1; i >= 0; i--) {
            if (tables.get(i).getTableNumber() == tableNumber) {
                tables.remove(i);
                removed = true;
            }
        }

        if (!removed) {
            throw new IllegalArgumentException("Table not found");
        }
    }

    public Table findTableByNumber(int tableNumber) {
        for (Table table : tables) {
            if (table.getTableNumber() == tableNumber) {
                return table;
            }
        }
        return null;
    }

    public List<Table> getAllTables() {
        return new ArrayList<>(tables);
    }

    private boolean isTableUsedInReservations(int tableNumber) {
        for (Reservation reservation : reservations) {
            if (reservation.getTable().getTableNumber() == tableNumber) {
                return true;
            }
        }
        return false;
    }

    public Reservation createReservation(String customerName, int tableNumber,
                                         LocalDateTime start, Duration duration) {
        Table table = requireTable(tableNumber);
        // Overlap checking stays in the service layer so CLI and GUI share one business rule.
        if (!isTableAvailable(tableNumber, start, duration)) {
            throw new IllegalArgumentException("Table is not available for this time range");
        }
        Reservation reservation = new Reservation(idGenerator.nextReservationId(), customerName, table, start, duration);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation findReservationById(int reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId() == reservationId) {
                return reservation;
            }
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public long countActiveReservations() {
        long count = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.CANCELLED
                    && reservation.getStatus() != ReservationStatus.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    public void confirmReservation(int reservationId) {
        requireReservation(reservationId).confirm();
    }

    public void cancelReservation(int reservationId) {
        requireReservation(reservationId).cancel();
    }

    public void completeReservation(int reservationId) {
        requireReservation(reservationId).complete();
    }

    public boolean isTableAvailable(int tableNumber, LocalDateTime start, Duration duration) {
        requireTable(tableNumber);

        for (Reservation reservation : reservations) {
            boolean sameTable = reservation.getTable().getTableNumber() == tableNumber;
            boolean activeReservation = reservation.getStatus() != ReservationStatus.CANCELLED
                    && reservation.getStatus() != ReservationStatus.COMPLETED;

            if (sameTable && activeReservation && reservation.overlapsWith(start, duration)) {
                return false;
            }
        }

        return true;
    }

    private boolean canLoadReservation(Reservation reservation) {
        if (findReservationById(reservation.getReservationId()) != null) {
            return false;
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            return true;
        }

        for (Reservation existing : reservations) {
            boolean sameTable = existing.getTable().getTableNumber()
                    == reservation.getTable().getTableNumber();
            boolean activeReservation = existing.getStatus() != ReservationStatus.CANCELLED
                    && existing.getStatus() != ReservationStatus.COMPLETED;

            if (sameTable && activeReservation
                    && existing.overlapsWith(reservation.getStartDateTime(), reservation.getDuration())) {
                return false;
            }
        }

        return true;
    }

    public CashPayment processCashPayment(int orderId, double cashReceived) {
        Order order = requirePayableOrder(orderId);
        CashPayment payment = new CashPayment(idGenerator.nextPaymentId(), order.calculateTotal(), cashReceived);
        payment.processPayment();
        order.setPayment(payment);
        return payment;
    }

    public CardPayment processCardPayment(int orderId, String cardNumber) {
        Order order = requirePayableOrder(orderId);
        CardPayment payment = new CardPayment(idGenerator.nextPaymentId(), order.calculateTotal(), cardNumber);
        payment.processPayment();
        order.setPayment(payment);
        return payment;
    }

    public double calculateSuccessfulPaymentIncome() {
        double income = 0;

        for (Order order : orders) {
            if (order.getPayment() != null
                    && order.getPayment().getStatus() == PaymentStatus.SUCCESSFUL) {
                income += order.getPayment().getAmount();
            }
        }

        return income;
    }

    public void saveData() {
        repository.saveMenuItems(menuItems);
        repository.saveTables(tables);
        repository.saveOrders(orders);
        repository.saveReservations(reservations);
    }

    public void loadData() {
        // Loaded records are passed through service-level consistency checks before use.
        menuItems = new ArrayList<>();
        for (MenuItem item : repository.loadMenuItems()) {
            if (findMenuItemById(item.getId()) == null) {
                menuItems.add(item);
            }
        }

        tables = new ArrayList<>();
        for (Table table : repository.loadTables()) {
            if (findTableByNumber(table.getTableNumber()) == null) {
                tables.add(table);
            }
        }

        orders = new ArrayList<>();
        for (Order order : repository.loadOrders(menuItems)) {
            if (findOrderById(order.getOrderId()) == null) {
                orders.add(order);
            }
        }

        reservations = new ArrayList<>();
        for (Reservation reservation : repository.loadReservations(tables)) {
            if (canLoadReservation(reservation)) {
                reservations.add(reservation);
            }
        }
        idGenerator.updateFromExistingData(menuItems, orders, reservations);
    }

    private MenuItem requireMenuItem(int id) {
        MenuItem item = findMenuItemById(id);
        if (item == null) {
            throw new IllegalArgumentException("Menu item not found");
        }
        return item;
    }

    private Order requireOrder(int orderId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    private Table requireTable(int tableNumber) {
        Table table = findTableByNumber(tableNumber);
        if (table == null) {
            throw new IllegalArgumentException("Table not found");
        }
        return table;
    }

    private Reservation requireReservation(int reservationId) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }
        return reservation;
    }

    private Order requirePayableOrder(int orderId) {
        Order order = requireOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled orders cannot be paid");
        }
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Empty orders cannot be paid");
        }
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.SUCCESSFUL) {
            throw new IllegalArgumentException("Order already has a successful payment");
        }
        return order;
    }
}
