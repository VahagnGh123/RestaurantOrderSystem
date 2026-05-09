package repository;

import java.util.List;

import model.MenuItem;
import model.order.Order;
import model.reservation.Reservation;
import model.reservation.Table;

public interface RestaurantRepository {
    void saveMenuItems(List<MenuItem> items);

    List<MenuItem> loadMenuItems();

    void saveOrders(List<Order> orders);

    List<Order> loadOrders(List<MenuItem> menuItems);

    void saveTables(List<Table> tables);

    List<Table> loadTables();

    void saveReservations(List<Reservation> reservations);

    List<Reservation> loadReservations(List<Table> tables);
}
