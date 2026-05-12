# Restaurant Management System

Java OOP project for managing a restaurant. It includes menu items, orders, tables, reservations, payments, discounts, file saving/loading, a console interface, and a Java Swing GUI.


## Features

- Menu management with add, edit, delete, search, category filter, availability, and discounts
- Order creation with multiple `OrderItem` rows
- Table management
- Time-based reservations with overlap prevention
- Cash and card payments
- Masked card numbers after processing
- File-based persistence using readable text files
- CLI mode and GUI mode

## Package Structure

```text
src/
  Main.java
  model/             data classes and small object behavior
  model/order/       Order, OrderItem, OrderStatus
  model/payment/     Payment, CashPayment, CardPayment
  model/reservation/ Table and Reservation classes
  repository/        file save/load classes
  service/           RestaurantManager business logic
  ui/cli/            console interface
  ui/gui/            Swing interface
  util/              validation, date/time, and ID helpers
data/                small sample text files
```

## OOP Ideas Used

- Encapsulation: private fields with validated constructors and setters
- Abstraction: abstract `Payment` class
- Inheritance: `CashPayment` and `CardPayment` extend `Payment`
- Polymorphism: each payment type implements `processPayment()` differently
- Interfaces: `Discountable` and `RestaurantRepository`
- Composition: `Order` contains `OrderItem`; `Reservation` contains `Table`
- Enums: categories and statuses

## Design Decisions

`FoodItem`, `DrinkItem`, and `DessertItem` are not separate classes because they would mostly duplicate the same fields and behavior. A single `MenuItem` with `MenuCategory` is simpler and cleaner.

`Table` does not have a permanent `isReserved` field because a table is reserved only for a specific time range. `Reservation` stores `LocalDateTime` and `Duration`, and `RestaurantManager` checks overlaps.


## Persistence

Data is stored in text files under `data/`:

- `menu.txt`
- `orders.txt`
- `tables.txt`
- `reservations.txt`



