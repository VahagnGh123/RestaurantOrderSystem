package ui.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.MenuCategory;
import model.MenuItem;
import model.order.Order;
import model.order.OrderItem;
import model.order.OrderStatus;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.payment.PaymentStatus;
import model.reservation.Reservation;
import model.reservation.Table;
import service.RestaurantManager;
import util.DateTimeUtil;

public class RestaurantGUI {
    private static final String ROW_SELECTION_ERROR = "Please select a row first.";
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color SIDEBAR = new Color(31, 41, 55);
    private static final Color SIDEBAR_ACTIVE = new Color(55, 65, 81);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final RestaurantManager manager;
    private JFrame frame;
    private CardLayout contentLayout;
    private JPanel contentPanel;

    private JLabel totalMenuItemsLabel;
    private JLabel availableMenuItemsLabel;
    private JLabel totalOrdersLabel;
    private JLabel pendingOrdersLabel;
    private JLabel totalTablesLabel;
    private JLabel activeReservationsLabel;
    private JLabel totalIncomeLabel;

    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    private JTextField menuSearchField;
    private JComboBox<String> menuCategoryFilter;

    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JTable orderItemsTable;
    private DefaultTableModel orderItemsTableModel;
    private JLabel selectedOrderTotalLabel;
    private JComboBox<OrderStatus> orderStatusCombo;

    private JTable tableTable;
    private DefaultTableModel tableTableModel;

    private JTable reservationTable;
    private DefaultTableModel reservationTableModel;

    private JComboBox<IdLabel> paymentOrderCombo;
    private JLabel paymentTotalLabel;
    private JRadioButton cashPaymentRadio;
    private JRadioButton cardPaymentRadio;
    private JTextField cashReceivedField;
    private JPasswordField cardNumberField;
    private JLabel paymentResultLabel;

    public RestaurantGUI(RestaurantManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Restaurant manager cannot be null");
        }
        this.manager = manager;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            frame = new JFrame("Restaurant Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1180, 760));
            frame.setLayout(new BorderLayout());
            frame.add(createHeader(), BorderLayout.NORTH);
            frame.add(createSidebar(), BorderLayout.WEST);
            frame.add(createMainContent(), BorderLayout.CENTER);
            refreshAll();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // The default Swing look and feel is acceptable if Nimbus is unavailable.
        }
    }

    private JToolBar createHeader() {
        JToolBar header = new JToolBar();
        header.setFloatable(false);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setBackground(PANEL_BACKGROUND);

        JLabel title = new JLabel("Restaurant Management System");
        title.setFont(TITLE_FONT);
        header.add(title);
        header.addSeparator(new Dimension(30, 1));

        JButton saveButton = primaryButton("Save Data");
        saveButton.addActionListener(event -> runAction("Save data", () -> {
            manager.saveData();
            refreshAll();
            showInfo("Data saved successfully.");
        }));
        header.add(saveButton);

        JButton loadButton = secondaryButton("Load Data");
        loadButton.addActionListener(event -> runAction("Load data", () -> {
            manager.loadData();
            refreshAll();
            showInfo("Data loaded successfully.");
        }));
        header.add(loadButton);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 6));
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(new EmptyBorder(18, 12, 18, 12));

        addNavigationButton(sidebar, "Dashboard", "dashboard");
        addNavigationButton(sidebar, "Menu Management", "menu");
        addNavigationButton(sidebar, "Order Management", "orders");
        addNavigationButton(sidebar, "Table Management", "tables");
        addNavigationButton(sidebar, "Reservations", "reservations");
        addNavigationButton(sidebar, "Payments", "payments");
        return sidebar;
    }

    private void addNavigationButton(JPanel sidebar, String title, String cardName) {
        JButton button = new JButton(title);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground("dashboard".equals(cardName) ? SIDEBAR_ACTIVE : SIDEBAR);
        button.setBorder(new EmptyBorder(12, 12, 12, 12));
        button.addActionListener(event -> {
            for (Component component : sidebar.getComponents()) {
                component.setBackground(SIDEBAR);
            }
            button.setBackground(SIDEBAR_ACTIVE);
            contentLayout.show(contentPanel, cardName);
            refreshAll();
        });
        sidebar.add(button);
    }

    private JPanel createMainContent() {
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createMenuPanel(), "menu");
        contentPanel.add(createOrdersPanel(), "orders");
        contentPanel.add(createTablesPanel(), "tables");
        contentPanel.add(createReservationsPanel(), "reservations");
        contentPanel.add(createPaymentsPanel(), "payments");
        return contentPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = pagePanel("Dashboard");
        panel.add(pageHeader("Dashboard"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
        grid.setOpaque(false);

        totalMenuItemsLabel = dashboardCard(grid, "Total Menu Items");
        availableMenuItemsLabel = dashboardCard(grid, "Available Menu Items");
        totalOrdersLabel = dashboardCard(grid, "Total Orders");
        pendingOrdersLabel = dashboardCard(grid, "Pending Orders");
        totalTablesLabel = dashboardCard(grid, "Total Tables");
        activeReservationsLabel = dashboardCard(grid, "Active Reservations");
        totalIncomeLabel = dashboardCard(grid, "Successful Payment Income");

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JLabel dashboardCard(JPanel parent, String title) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(PANEL_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BODY_FONT);
        titleLabel.setForeground(new Color(71, 85, 105));
        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(new Color(15, 23, 42));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        parent.add(card);
        return valueLabel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = pagePanel("Menu Management");
        JPanel controls = actionBar();

        menuSearchField = new JTextField(20);
        menuSearchField.addActionListener(event -> refreshMenuTable());
        controls.add(new JLabel("Search"));
        controls.add(menuSearchField);

        menuCategoryFilter = new JComboBox<>(new String[]{"ALL", "FOOD", "DRINK", "DESSERT", "OTHER"});
        menuCategoryFilter.addActionListener(event -> refreshMenuTable());
        controls.add(new JLabel("Category"));
        controls.add(menuCategoryFilter);

        JButton searchButton = secondaryButton("Filter");
        searchButton.addActionListener(event -> refreshMenuTable());
        controls.add(searchButton);

        JButton addButton = primaryButton("Add Menu Item");
        addButton.addActionListener(event -> showMenuItemDialog(null));
        controls.add(addButton);

        JButton editButton = secondaryButton("Edit");
        editButton.addActionListener(event -> {
            MenuItem selected = getSelectedMenuItem();
            if (selected != null) {
                showMenuItemDialog(selected);
            }
        });
        controls.add(editButton);

        JButton deleteButton = dangerButton("Delete");
        deleteButton.addActionListener(event -> deleteSelectedMenuItem());
        controls.add(deleteButton);

        JButton discountButton = secondaryButton("Apply Discount");
        discountButton.addActionListener(event -> applyDiscountToSelectedMenuItem());
        controls.add(discountButton);

        JButton availabilityButton = secondaryButton("Toggle Available");
        availabilityButton.addActionListener(event -> toggleSelectedMenuItemAvailability());
        controls.add(availabilityButton);

        panel.add(pageHeader("Menu Management", controls), BorderLayout.NORTH);

        menuTableModel = tableModel("ID", "Name", "Price", "Category", "Available",
                "Calories", "Volume", "Sugar", "Description");
        menuTable = table(menuTableModel);
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = pagePanel("Order Management");

        JPanel controls = actionBar();
        JButton createOrderButton = primaryButton("Create New Order");
        createOrderButton.addActionListener(event -> runAction("Create order", () -> {
            Order order = manager.createOrder();
            refreshAll();
            selectOrderInTable(order.getOrderId());
        }));
        controls.add(createOrderButton);

        JButton addItemButton = secondaryButton("Add Menu Item");
        addItemButton.addActionListener(event -> addMenuItemToSelectedOrder());
        controls.add(addItemButton);

        JButton updateQuantityButton = secondaryButton("Update Quantity");
        updateQuantityButton.addActionListener(event -> updateSelectedOrderItemQuantity());
        controls.add(updateQuantityButton);

        JButton removeItemButton = dangerButton("Remove Item");
        removeItemButton.addActionListener(event -> removeSelectedOrderItem());
        controls.add(removeItemButton);

        orderStatusCombo = new JComboBox<>(OrderStatus.values());
        controls.add(new JLabel("Status"));
        controls.add(orderStatusCombo);

        JButton statusButton = secondaryButton("Change Status");
        statusButton.addActionListener(event -> updateSelectedOrderStatus());
        controls.add(statusButton);
        panel.add(pageHeader("Order Management", controls), BorderLayout.NORTH);

        JPanel split = new JPanel(new GridLayout(2, 1, 0, 14));
        split.setOpaque(false);

        ordersTableModel = tableModel("Order ID", "Status", "Created At", "Items", "Total", "Payment");
        ordersTable = table(ordersTableModel);
        ordersTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                refreshOrderItemsTable();
            }
        });
        split.add(titledPanel("Orders", new JScrollPane(ordersTable)));

        orderItemsTableModel = tableModel("Menu Item ID", "Name", "Quantity", "Unit Price", "Line Total");
        orderItemsTable = table(orderItemsTableModel);
        JPanel itemsPanel = new JPanel(new BorderLayout(8, 8));
        itemsPanel.setOpaque(false);
        selectedOrderTotalLabel = new JLabel("Selected order total: 0 AMD");
        selectedOrderTotalLabel.setFont(SECTION_FONT);
        itemsPanel.add(selectedOrderTotalLabel, BorderLayout.NORTH);
        itemsPanel.add(new JScrollPane(orderItemsTable), BorderLayout.CENTER);
        split.add(titledPanel("Order Items", itemsPanel));

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablesPanel() {
        JPanel panel = pagePanel("Table Management");
        JPanel controls = actionBar();

        JButton addButton = primaryButton("Add Table");
        addButton.addActionListener(event -> showTableDialog());
        controls.add(addButton);

        JButton deleteButton = dangerButton("Delete Table");
        deleteButton.addActionListener(event -> deleteSelectedTable());
        controls.add(deleteButton);

        panel.add(pageHeader("Table Management", controls), BorderLayout.NORTH);

        tableTableModel = tableModel("Table Number", "Capacity");
        tableTable = table(tableTableModel);
        panel.add(new JScrollPane(tableTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReservationsPanel() {
        JPanel panel = pagePanel("Reservation Management");
        JPanel controls = actionBar();

        JButton createButton = primaryButton("Create Reservation");
        createButton.addActionListener(event -> showReservationDialog());
        controls.add(createButton);

        JButton confirmButton = secondaryButton("Confirm");
        confirmButton.addActionListener(event -> updateSelectedReservation("Confirm", manager::confirmReservation));
        controls.add(confirmButton);

        JButton cancelButton = dangerButton("Cancel");
        cancelButton.addActionListener(event -> updateSelectedReservation("Cancel", manager::cancelReservation));
        controls.add(cancelButton);

        JButton completeButton = secondaryButton("Complete");
        completeButton.addActionListener(event -> updateSelectedReservation("Complete", manager::completeReservation));
        controls.add(completeButton);

        panel.add(pageHeader("Reservation Management", controls), BorderLayout.NORTH);

        reservationTableModel = tableModel("ID", "Customer", "Table", "Start", "Duration", "End", "Status");
        reservationTable = table(reservationTableModel);
        panel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPaymentsPanel() {
        JPanel panel = pagePanel("Payment Management");
        panel.add(pageHeader("Payment Management"), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BACKGROUND);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Order"), gbc);

        paymentOrderCombo = new JComboBox<>();
        paymentOrderCombo.addActionListener(event -> updatePaymentTotal());
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(paymentOrderCombo, gbc);

        paymentTotalLabel = new JLabel("Total: 0 AMD");
        paymentTotalLabel.setFont(SECTION_FONT);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(paymentTotalLabel, gbc);

        cashPaymentRadio = new JRadioButton("Cash", true);
        cardPaymentRadio = new JRadioButton("Card");
        cashPaymentRadio.setBackground(PANEL_BACKGROUND);
        cardPaymentRadio.setBackground(PANEL_BACKGROUND);
        ButtonGroup paymentTypeGroup = new ButtonGroup();
        paymentTypeGroup.add(cashPaymentRadio);
        paymentTypeGroup.add(cardPaymentRadio);

        JPanel paymentTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        paymentTypePanel.setOpaque(false);
        paymentTypePanel.add(cashPaymentRadio);
        paymentTypePanel.add(cardPaymentRadio);
        cashPaymentRadio.addItemListener(event -> updatePaymentFields());
        cardPaymentRadio.addItemListener(event -> updatePaymentFields());
        gbc.gridy++;
        form.add(paymentTypePanel, gbc);

        cashReceivedField = new JTextField(16);
        cardNumberField = new JPasswordField(16);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Cash Received"), gbc);
        gbc.gridx = 1;
        form.add(cashReceivedField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Card Number"), gbc);
        gbc.gridx = 1;
        form.add(cardNumberField, gbc);

        JButton processButton = primaryButton("Process Payment");
        processButton.addActionListener(event -> processSelectedPayment());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(processButton, gbc);

        paymentResultLabel = new JLabel(" ");
        paymentResultLabel.setFont(SECTION_FONT);
        gbc.gridy++;
        form.add(paymentResultLabel, gbc);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    public void refreshDashboard() {
        List<MenuItem> menuItems = manager.getAllMenuItems();
        List<Order> orders = manager.getAllOrders();
        List<Table> tables = manager.getAllTables();
        totalMenuItemsLabel.setText(String.valueOf(menuItems.size()));
        availableMenuItemsLabel.setText(String.valueOf(manager.getAvailableMenuItems().size()));
        totalOrdersLabel.setText(String.valueOf(orders.size()));
        pendingOrdersLabel.setText(String.valueOf(manager.countOrdersByStatus(OrderStatus.PENDING)));
        totalTablesLabel.setText(String.valueOf(tables.size()));
        activeReservationsLabel.setText(String.valueOf(manager.countActiveReservations()));
        totalIncomeLabel.setText(formatMoney(manager.calculateSuccessfulPaymentIncome()));
    }

    public void refreshMenuTable() {
        menuTableModel.setRowCount(0);
        String searchText = menuSearchField == null ? "" : menuSearchField.getText().trim().toLowerCase();
        String selectedCategory = menuCategoryFilter == null ? "ALL" : String.valueOf(menuCategoryFilter.getSelectedItem());

        for (MenuItem item : manager.getAllMenuItems()) {
            boolean matchesSearch = searchText.isEmpty() || item.getName().toLowerCase().contains(searchText);
            boolean matchesCategory = "ALL".equals(selectedCategory) || item.getCategory().name().equals(selectedCategory);
            if (matchesSearch && matchesCategory) {
                menuTableModel.addRow(new Object[]{
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getCategory(),
                        item.isAvailable(),
                        item.getCalories(),
                        item.getVolume(),
                        item.getSugarLevel(),
                        item.getDescription()
                });
            }
        }
    }

    public void refreshOrderTable() {
        ordersTableModel.setRowCount(0);
        for (Order order : manager.getAllOrders()) {
            String paymentStatus = order.getPayment() == null ? "No payment" : order.getPayment().getStatus().name();
            ordersTableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getStatus(),
                    DateTimeUtil.formatDateTime(order.getCreatedAt()),
                    order.getItems().size(),
                    formatMoney(manager.calculateOrderTotal(order.getOrderId())),
                    paymentStatus
            });
        }
        refreshPaymentOrderCombo();
    }

    public void refreshOrderItemsTable() {
        orderItemsTableModel.setRowCount(0);
        Order selectedOrder = getSelectedOrderSilently();
        if (selectedOrder == null) {
            selectedOrderTotalLabel.setText("Selected order total: 0 AMD");
            return;
        }
        for (OrderItem orderItem : selectedOrder.getItems()) {
            MenuItem item = orderItem.getItem();
            orderItemsTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    orderItem.getQuantity(),
                    formatMoney(item.getPrice()),
                    formatMoney(orderItem.getTotalPrice())
            });
        }
        selectedOrderTotalLabel.setText("Selected order total: "
                + formatMoney(manager.calculateOrderTotal(selectedOrder.getOrderId())));
    }

    public void refreshTableTable() {
        tableTableModel.setRowCount(0);
        for (Table table : manager.getAllTables()) {
            tableTableModel.addRow(new Object[]{table.getTableNumber(), table.getCapacity()});
        }
    }

    public void refreshReservationTable() {
        reservationTableModel.setRowCount(0);
        for (Reservation reservation : manager.getAllReservations()) {
            reservationTableModel.addRow(new Object[]{
                    reservation.getReservationId(),
                    reservation.getCustomerName(),
                    reservation.getTable().getTableNumber(),
                    DateTimeUtil.formatDateTime(reservation.getStartDateTime()),
                    formatDuration(reservation.getDuration()),
                    DateTimeUtil.formatDateTime(reservation.getEndDateTime()),
                    reservation.getStatus()
            });
        }
    }

    private void refreshAll() {
        if (totalMenuItemsLabel != null) {
            refreshDashboard();
        }
        if (menuTableModel != null) {
            refreshMenuTable();
        }
        if (ordersTableModel != null) {
            int selectedOrderId = getSelectedOrderIdOrZero();
            refreshOrderTable();
            if (selectedOrderId > 0) {
                selectOrderInTable(selectedOrderId);
            }
            refreshOrderItemsTable();
        }
        if (tableTableModel != null) {
            refreshTableTable();
        }
        if (reservationTableModel != null) {
            refreshReservationTable();
        }
        if (paymentOrderCombo != null) {
            refreshPaymentOrderCombo();
            updatePaymentTotal();
            updatePaymentFields();
        }
    }

    private void showMenuItemDialog(MenuItem existingItem) {
        MenuItemForm form = new MenuItemForm(existingItem);
        String title = existingItem == null ? "Add Menu Item" : "Edit Menu Item";
        int result = JOptionPane.showConfirmDialog(frame, form.getPanel(), title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        runAction(title, () -> {
            MenuCategory category = form.getCategory();
            String name = form.getName();
            double price = form.getPrice();
            boolean available = form.isAvailable();
            int calories = form.getCalories();
            double volume = form.getVolume();
            int sugarLevel = form.getSugarLevel();
            if (existingItem == null) {
                manager.createMenuItem(name, price, category, available, calories, volume, sugarLevel);
            } else {
                manager.updateMenuItem(existingItem.getId(), name, price, category, available,
                        calories, volume, sugarLevel);
            }
            refreshAll();
        });
    }

    private void deleteSelectedMenuItem() {
        MenuItem selected = getSelectedMenuItem();
        if (selected == null) {
            return;
        }
        if (!confirm("Delete menu item '" + selected.getName() + "'?")) {
            return;
        }
        runAction("Delete menu item", () -> {
            manager.removeMenuItemById(selected.getId());
            refreshAll();
        });
    }

    private void applyDiscountToSelectedMenuItem() {
        MenuItem selected = getSelectedMenuItem();
        if (selected == null) {
            return;
        }
        String value = JOptionPane.showInputDialog(frame, "Discount percent (0-100):", "10");
        if (value == null) {
            return;
        }
        runAction("Apply discount", () -> {
            manager.applyDiscountToMenuItem(selected.getId(), parseDouble(value, "Discount percent"));
            refreshAll();
        });
    }

    private void toggleSelectedMenuItemAvailability() {
        MenuItem selected = getSelectedMenuItem();
        if (selected == null) {
            return;
        }
        runAction("Toggle availability", () -> {
            manager.setMenuItemAvailability(selected.getId(), !selected.isAvailable());
            refreshAll();
        });
    }

    private void addMenuItemToSelectedOrder() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder == null) {
            return;
        }
        JComboBox<IdLabel> itemCombo = new JComboBox<>();
        for (MenuItem item : manager.getAvailableMenuItems()) {
            itemCombo.addItem(new IdLabel(item.getId(), item.getName() + " - " + formatMoney(item.getPrice())));
        }
        if (itemCombo.getItemCount() == 0) {
            showError("There are no available menu items to add.");
            return;
        }
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JPanel form = simpleForm(new String[]{"Menu Item", "Quantity"}, new Component[]{itemCombo, quantitySpinner});
        int result = JOptionPane.showConfirmDialog(frame, form, "Add Item To Order",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION || itemCombo.getSelectedItem() == null) {
            return;
        }

        runAction("Add item to order", () -> {
            IdLabel selectedItem = (IdLabel) itemCombo.getSelectedItem();
            manager.addItemToOrder(selectedOrder.getOrderId(), selectedItem.id, (Integer) quantitySpinner.getValue());
            refreshAll();
            selectOrderInTable(selectedOrder.getOrderId());
        });
    }

    private void updateSelectedOrderItemQuantity() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder == null) {
            return;
        }
        Integer menuItemId = getSelectedOrderItemMenuId();
        if (menuItemId == null) {
            return;
        }
        String value = JOptionPane.showInputDialog(frame, "New quantity:", "1");
        if (value == null) {
            return;
        }
        runAction("Update quantity", () -> {
            manager.updateOrderItemQuantity(selectedOrder.getOrderId(), menuItemId, parseInt(value, "Quantity"));
            refreshAll();
            selectOrderInTable(selectedOrder.getOrderId());
        });
    }

    private void removeSelectedOrderItem() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder == null) {
            return;
        }
        Integer menuItemId = getSelectedOrderItemMenuId();
        if (menuItemId == null) {
            return;
        }
        if (!confirm("Remove selected item from order?")) {
            return;
        }
        runAction("Remove item from order", () -> {
            manager.removeItemFromOrder(selectedOrder.getOrderId(), menuItemId);
            refreshAll();
            selectOrderInTable(selectedOrder.getOrderId());
        });
    }

    private void updateSelectedOrderStatus() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder == null) {
            return;
        }
        OrderStatus status = (OrderStatus) orderStatusCombo.getSelectedItem();
        runAction("Update order status", () -> {
            manager.updateOrderStatus(selectedOrder.getOrderId(), status);
            refreshAll();
            selectOrderInTable(selectedOrder.getOrderId());
        });
    }

    private void showTableDialog() {
        JSpinner numberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        JPanel form = simpleForm(new String[]{"Table Number", "Capacity"}, new Component[]{numberSpinner, capacitySpinner});
        int result = JOptionPane.showConfirmDialog(frame, form, "Add Table",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        runAction("Add table", () -> {
            manager.createTable((Integer) numberSpinner.getValue(), (Integer) capacitySpinner.getValue());
            refreshAll();
        });
    }

    private void deleteSelectedTable() {
        Integer tableNumber = getSelectedTableNumber();
        if (tableNumber == null) {
            return;
        }
        if (!confirm("Delete table #" + tableNumber + "?")) {
            return;
        }
        runAction("Delete table", () -> {
            manager.removeTableByNumber(tableNumber);
            refreshAll();
        });
    }

    private void showReservationDialog() {
        JTextField customerField = new JTextField(18);
        JComboBox<IdLabel> tableCombo = new JComboBox<>();
        for (Table table : manager.getAllTables()) {
            tableCombo.addItem(new IdLabel(table.getTableNumber(),
                    "Table " + table.getTableNumber() + " (capacity " + table.getCapacity() + ")"));
        }
        JTextField startField = new JTextField(LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' '), 18);
        JSpinner hoursSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 24, 1));
        JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));

        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        durationPanel.setOpaque(false);
        durationPanel.add(hoursSpinner);
        durationPanel.add(new JLabel("hours"));
        durationPanel.add(minutesSpinner);
        durationPanel.add(new JLabel("minutes"));

        JPanel form = simpleForm(
                new String[]{"Customer", "Table", "Start (yyyy-MM-dd HH:mm)", "Duration"},
                new Component[]{customerField, tableCombo, startField, durationPanel});

        int result = JOptionPane.showConfirmDialog(frame, form, "Create Reservation",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (tableCombo.getSelectedItem() == null) {
            showError("Please add a table before creating a reservation.");
            return;
        }

        runAction("Create reservation", () -> {
            IdLabel selectedTable = (IdLabel) tableCombo.getSelectedItem();
            long minutes = ((Integer) hoursSpinner.getValue()) * 60L + (Integer) minutesSpinner.getValue();
            manager.createReservation(
                    customerField.getText(),
                    selectedTable.id,
                    DateTimeUtil.parseDateTime(startField.getText().trim()),
                    Duration.ofMinutes(minutes));
            refreshAll();
        });
    }

    private void updateSelectedReservation(String actionName, IntConsumer action) {
        int reservationId = getSelectedReservationId();
        if (reservationId <= 0) {
            return;
        }
        runAction(actionName + " reservation", () -> {
            action.accept(reservationId);
            refreshAll();
        });
    }

    private void processSelectedPayment() {
        IdLabel selectedOrder = (IdLabel) paymentOrderCombo.getSelectedItem();
        if (selectedOrder == null) {
            showError("Please create an order first.");
            return;
        }
        runAction("Process payment", () -> {
            if (cashPaymentRadio.isSelected()) {
                CashPayment payment = manager.processCashPayment(
                        selectedOrder.id,
                        parseDouble(cashReceivedField.getText(), "Cash received"));
                paymentResultLabel.setText("Status: " + payment.getStatus()
                        + " | Change: " + formatMoney(payment.calculateChange()));
            } else {
                String cardNumber = new String(cardNumberField.getPassword());
                CardPayment payment = manager.processCardPayment(selectedOrder.id, cardNumber);
                cardNumberField.setText("");
                paymentResultLabel.setText("Status: " + payment.getStatus()
                        + " | Card: " + payment.getMaskedCardNumber());
            }
            refreshAll();
            if (paymentResultLabel.getText().contains(PaymentStatus.SUCCESSFUL.name())) {
                showInfo("Payment processed successfully.");
            } else {
                showInfo("Payment was processed with a failed status.");
            }
        });
    }

    private void refreshPaymentOrderCombo() {
        if (paymentOrderCombo == null) {
            return;
        }
        int selectedId = paymentOrderCombo.getSelectedItem() instanceof IdLabel
                ? ((IdLabel) paymentOrderCombo.getSelectedItem()).id
                : 0;
        DefaultComboBoxModel<IdLabel> model = new DefaultComboBoxModel<>();
        for (Order order : manager.getAllOrders()) {
            model.addElement(new IdLabel(order.getOrderId(),
                    "Order #" + order.getOrderId() + " - " + order.getStatus()));
        }
        paymentOrderCombo.setModel(model);
        if (selectedId > 0) {
            selectComboItem(paymentOrderCombo, selectedId);
        }
    }

    private void updatePaymentTotal() {
        if (paymentOrderCombo == null || paymentTotalLabel == null) {
            return;
        }
        IdLabel selectedOrder = (IdLabel) paymentOrderCombo.getSelectedItem();
        if (selectedOrder == null) {
            paymentTotalLabel.setText("Total: 0 AMD");
            return;
        }
        paymentTotalLabel.setText("Total: " + formatMoney(manager.calculateOrderTotal(selectedOrder.id)));
    }

    private void updatePaymentFields() {
        if (cashReceivedField == null || cardNumberField == null) {
            return;
        }
        boolean cashSelected = cashPaymentRadio.isSelected();
        cashReceivedField.setEnabled(cashSelected);
        cardNumberField.setEnabled(!cashSelected);
    }

    private JPanel pagePanel(String title) {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));
        return page;
    }

    private JPanel pageHeader(String title) {
        return pageHeader(title, null);
    }

    private JPanel pageHeader(String title, JPanel controls) {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        header.add(titleLabel, BorderLayout.NORTH);
        if (controls != null) {
            header.add(controls, BorderLayout.CENTER);
        }
        return header;
    }

    private JPanel actionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    private JPanel titledPanel(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BACKGROUND);
        JLabel label = new JLabel(title);
        label.setFont(SECTION_FONT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(BODY_FONT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        return table;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(226, 232, 240));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(220, 38, 38));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private JPanel simpleForm(String[] labels, Component[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(fields[i], gbc);
        }
        return panel;
    }

    private MenuItem getSelectedMenuItem() {
        Integer itemId = getSelectedId(menuTable, 0, true);
        return itemId == null ? null : manager.findMenuItemById(itemId);
    }

    private Order getSelectedOrder() {
        Integer orderId = getSelectedId(ordersTable, 0, true);
        return orderId == null ? null : manager.findOrderById(orderId);
    }

    private Order getSelectedOrderSilently() {
        Integer orderId = getSelectedId(ordersTable, 0, false);
        return orderId == null ? null : manager.findOrderById(orderId);
    }

    private int getSelectedOrderIdOrZero() {
        if (ordersTable == null || ordersTable.getSelectedRow() < 0) {
            return 0;
        }
        int modelRow = ordersTable.convertRowIndexToModel(ordersTable.getSelectedRow());
        return (Integer) ordersTableModel.getValueAt(modelRow, 0);
    }

    private Integer getSelectedOrderItemMenuId() {
        return getSelectedId(orderItemsTable, 0, true);
    }

    private Integer getSelectedTableNumber() {
        return getSelectedId(tableTable, 0, true);
    }

    private int getSelectedReservationId() {
        Integer reservationId = getSelectedId(reservationTable, 0, true);
        return reservationId == null ? 0 : reservationId;
    }

    private Integer getSelectedId(JTable table, int column, boolean showMessage) {
        if (table == null || table.getSelectedRow() < 0) {
            if (showMessage) {
                showError(ROW_SELECTION_ERROR);
            }
            return null;
        }
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        Object value = table.getModel().getValueAt(modelRow, column);
        return value instanceof Integer ? (Integer) value : Integer.parseInt(String.valueOf(value));
    }

    private void selectOrderInTable(int orderId) {
        for (int row = 0; row < ordersTableModel.getRowCount(); row++) {
            if ((Integer) ordersTableModel.getValueAt(row, 0) == orderId) {
                int viewRow = ordersTable.convertRowIndexToView(row);
                ordersTable.setRowSelectionInterval(viewRow, viewRow);
                ordersTable.scrollRectToVisible(ordersTable.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void selectComboItem(JComboBox<IdLabel> comboBox, int id) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == id) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void runAction(String actionName, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            showError(actionName + " failed: " + exception.getMessage());
        }
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(frame, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Restaurant Management System",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatMoney(double amount) {
        return String.format("%.0f AMD", amount);
    }

    private String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (remainingMinutes == 0) {
            return hours + "h";
        }
        return hours + "h " + remainingMinutes + "m";
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a valid integer");
        }
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private final class MenuItemForm {
        private final JPanel panel;
        private final JTextField nameField;
        private final JTextField priceField;
        private final JComboBox<MenuCategory> categoryCombo;
        private final JCheckBox availableCheckBox;
        private final JTextField caloriesField;
        private final JTextField volumeField;
        private final JTextField sugarLevelField;

        private MenuItemForm(MenuItem item) {
            nameField = new JTextField(item == null ? "" : item.getName(), 18);
            priceField = new JTextField(item == null ? "" : String.valueOf(item.getPrice()), 18);
            categoryCombo = new JComboBox<>(MenuCategory.values());
            availableCheckBox = new JCheckBox("Available", item == null || item.isAvailable());
            caloriesField = new JTextField(item == null ? "0" : String.valueOf(item.getCalories()), 18);
            volumeField = new JTextField(item == null ? "0.0" : String.valueOf(item.getVolume()), 18);
            sugarLevelField = new JTextField(item == null ? "0" : String.valueOf(item.getSugarLevel()), 18);
            if (item != null) {
                categoryCombo.setSelectedItem(item.getCategory());
            }
            availableCheckBox.setBackground(Color.WHITE);

            panel = simpleForm(
                    new String[]{"Name", "Price", "Category", "Available", "Calories", "Volume (L)", "Sugar Level"},
                    new Component[]{nameField, priceField, categoryCombo, availableCheckBox,
                            caloriesField, volumeField, sugarLevelField});

            categoryCombo.addItemListener(event -> {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    updateOptionalFields();
                }
            });
            updateOptionalFields();
        }

        private JPanel getPanel() {
            return panel;
        }

        private String getName() {
            return nameField.getText();
        }

        private double getPrice() {
            return parseDouble(priceField.getText(), "Price");
        }

        private MenuCategory getCategory() {
            return (MenuCategory) categoryCombo.getSelectedItem();
        }

        private boolean isAvailable() {
            return availableCheckBox.isSelected();
        }

        private int getCalories() {
            return getCategory() == MenuCategory.FOOD ? parseInt(caloriesField.getText(), "Calories") : 0;
        }

        private double getVolume() {
            return getCategory() == MenuCategory.DRINK ? parseDouble(volumeField.getText(), "Volume") : 0.0;
        }

        private int getSugarLevel() {
            return getCategory() == MenuCategory.DESSERT ? parseInt(sugarLevelField.getText(), "Sugar level") : 0;
        }

        private void updateOptionalFields() {
            MenuCategory category = getCategory();
            caloriesField.setEnabled(category == MenuCategory.FOOD);
            volumeField.setEnabled(category == MenuCategory.DRINK);
            sugarLevelField.setEnabled(category == MenuCategory.DESSERT);
        }
    }

    private static final class IdLabel {
        private final int id;
        private final String label;

        private IdLabel(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
