package repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import model.MenuItem;
import model.order.Order;
import model.reservation.Reservation;
import model.reservation.Table;

public class FileRestaurantRepository implements RestaurantRepository {
    private final String menuFilePath;
    private final String ordersFilePath;
    private final String tablesFilePath;
    private final String reservationsFilePath;

    public FileRestaurantRepository(String menuFilePath, String ordersFilePath,
                                    String tablesFilePath, String reservationsFilePath) {
        this.menuFilePath = menuFilePath;
        this.ordersFilePath = ordersFilePath;
        this.tablesFilePath = tablesFilePath;
        this.reservationsFilePath = reservationsFilePath;
    }

    @Override
    public void saveMenuItems(List<MenuItem> items) {
        saveLines(menuFilePath, items.stream().map(MenuItem::toStorageString).collect(Collectors.toList()));
    }

    @Override
    public List<MenuItem> loadMenuItems() {
        return loadLines(menuFilePath, MenuItem::fromStorageString);
    }

    @Override
    public void saveOrders(List<Order> orders) {
        saveLines(ordersFilePath, orders.stream().map(Order::toStorageString).collect(Collectors.toList()));
    }

    @Override
    public List<Order> loadOrders(List<MenuItem> menuItems) {
        return loadLines(ordersFilePath, line -> Order.fromStorageString(line, menuItems));
    }

    @Override
    public void saveTables(List<Table> tables) {
        saveLines(tablesFilePath, tables.stream().map(Table::toStorageString).collect(Collectors.toList()));
    }

    @Override
    public List<Table> loadTables() {
        return loadLines(tablesFilePath, Table::fromStorageString);
    }

    @Override
    public void saveReservations(List<Reservation> reservations) {
        saveLines(reservationsFilePath,
                reservations.stream().map(Reservation::toStorageString).collect(Collectors.toList()));
    }

    @Override
    public List<Reservation> loadReservations(List<Table> tables) {
        return loadLines(reservationsFilePath, line -> Reservation.fromStorageString(line, tables));
    }

    private void saveLines(String filePath, List<String> lines) {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save data to " + filePath, exception);
        }
    }

    private <T> List<T> loadLines(String filePath, Function<String, T> parser) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(path)) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    result.add(parser.apply(line));
                } catch (RuntimeException exception) {
                    // One damaged row should not prevent the rest of the restaurant data from loading.
                    System.err.println("Skipping invalid line in " + filePath + ": " + line);
                }
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load data from " + filePath, exception);
        }
    }
}
