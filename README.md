# Restaurant Management System

A plain Java OOP project for managing a restaurant. It includes menu items, orders, tables, reservations, payments, discounts, file saving/loading, a console interface, and a Java Swing GUI.

The project is separated into layers so the UI does not contain business logic. Both the CLI and GUI call the same `RestaurantManager`.

## Features

- Menu management with add, edit, delete, search, category filter, availability, and discounts
- One `MenuItem` class with `MenuCategory` instead of unnecessary subclasses
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
- Enums: categories and statuses avoid magic strings

## Design Decisions

`FoodItem`, `DrinkItem`, and `DessertItem` are not separate classes because they would mostly duplicate the same fields and behavior. A single `MenuItem` with `MenuCategory` is simpler and cleaner.

`Table` does not have a permanent `isReserved` field because a table is reserved only for a specific time range. `Reservation` stores `LocalDateTime` and `Duration`, and `RestaurantManager` checks overlaps.

The CLI and GUI avoid duplicated logic by calling `RestaurantManager` for all operations such as creating orders, applying discounts, checking reservations, processing payments, and saving/loading.

## Compile and Run

Windows PowerShell:

```powershell
javac -Xlint:all -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
java -cp out Main gui
```

macOS/Linux:

```bash
javac -Xlint:all -d out $(find src -name "*.java")
java -cp out Main
java -cp out Main gui
```

## Persistence

Data is stored in text files under `data/`:

- `menu.txt`
- `orders.txt`
- `tables.txt`
- `reservations.txt`

Missing files are treated as empty data. Invalid lines are skipped so one bad line does not crash the whole program.


