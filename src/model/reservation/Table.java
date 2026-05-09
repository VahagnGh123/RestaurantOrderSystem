package model.reservation;

import util.InputValidator;

public class Table {
    private final int tableNumber;
    private final int capacity;

    public Table(int tableNumber, int capacity) {
        InputValidator.requirePositive(tableNumber, "Table number");
        InputValidator.requirePositive(capacity, "Table capacity");
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public String toStorageString() {
        return "TABLE;" + tableNumber + ";" + capacity;
    }

    public static Table fromStorageString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3 || !"TABLE".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid table storage line");
        }
        return new Table(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    @Override
    public String toString() {
        return "Table #" + tableNumber + " - capacity " + capacity;
    }
}
