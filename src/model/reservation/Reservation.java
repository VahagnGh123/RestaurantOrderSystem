package model.reservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import util.InputValidator;

public class Reservation {
    private final int reservationId;
    private final String customerName;
    private final Table table;
    private final LocalDateTime startDateTime;
    private final Duration duration;
    private ReservationStatus status;

    public Reservation(int reservationId, String customerName, Table table,
                       LocalDateTime startDateTime, Duration duration) {
        this(reservationId, customerName, table, startDateTime, duration, ReservationStatus.PENDING);
    }

    private Reservation(int reservationId, String customerName, Table table,
                        LocalDateTime startDateTime, Duration duration, ReservationStatus status) {
        InputValidator.requirePositive(reservationId, "Reservation id");
        InputValidator.requireNonEmpty(customerName, "Customer name");
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (startDateTime == null) {
            throw new IllegalArgumentException("Reservation start time cannot be null");
        }
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Reservation duration must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Reservation status cannot be null");
        }
        this.reservationId = reservationId;
        this.customerName = customerName.trim();
        this.table = table;
        this.startDateTime = startDateTime;
        this.duration = duration;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Table getTable() {
        return table;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plus(duration);
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void confirm() {
        status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        status = ReservationStatus.CANCELLED;
    }

    public void complete() {
        status = ReservationStatus.COMPLETED;
    }

    public boolean overlapsWith(LocalDateTime start, Duration duration) {
        if (start == null || duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Start time and duration must be valid");
        }
        // Tables are reserved for time ranges, so overlap is calculated from start and end times.
        LocalDateTime end = start.plus(duration);
        return start.isBefore(getEndDateTime()) && end.isAfter(startDateTime);
    }

    public String toStorageString() {
        return String.join(";",
                "RESERVATION",
                String.valueOf(reservationId),
                customerName,
                String.valueOf(table.getTableNumber()),
                startDateTime.toString(),
                duration.toString(),
                status.name());
    }

    public static Reservation fromStorageString(String line, List<Table> tables) {
        String[] parts = line.split(";", -1);
        if (parts.length != 7 || !"RESERVATION".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid reservation storage line");
        }

        int tableNumber = Integer.parseInt(parts[3]);
        Table table = tables.stream()
                .filter(existing -> existing.getTableNumber() == tableNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation references missing table"));

        return new Reservation(
                Integer.parseInt(parts[1]),
                parts[2],
                table,
                LocalDateTime.parse(parts[4]),
                Duration.parse(parts[5]),
                ReservationStatus.valueOf(parts[6]));
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " - " + customerName + " - table "
                + table.getTableNumber() + " - " + startDateTime + " - " + status;
    }
}
