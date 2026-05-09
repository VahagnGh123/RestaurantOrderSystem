package model;

import util.InputValidator;

public class MenuItem implements Discountable {
    private final int id;
    private String name;
    private double price;
    private MenuCategory category;
    private boolean available;
    private int calories;
    private double volume;
    private int sugarLevel;

    // A category enum keeps the assignment model simple without almost-empty subclasses.
    public MenuItem(int id, String name, double price, MenuCategory category, boolean available,
                    int calories, double volume, int sugarLevel) {
        InputValidator.requirePositive(id, "Menu item id");
        InputValidator.requireNonEmpty(name, "Menu item name");
        InputValidator.requireNonNegative(price, "Menu item price");
        if (category == null) {
            throw new IllegalArgumentException("Menu category cannot be null");
        }
        InputValidator.requireNonNegative(calories, "Calories");
        InputValidator.requireNonNegative(volume, "Volume");
        InputValidator.requireSugarLevel(sugarLevel);
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.category = category;
        this.available = available;
        this.calories = calories;
        this.volume = volume;
        this.sugarLevel = sugarLevel;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        InputValidator.requireNonEmpty(name, "Menu item name");
        this.name = name.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        InputValidator.requireNonNegative(price, "Menu item price");
        this.price = price;
    }

    public MenuCategory getCategory() {
        return category;
    }

    public void setCategory(MenuCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Menu category cannot be null");
        }
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        InputValidator.requireNonNegative(calories, "Calories");
        this.calories = calories;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        InputValidator.requireNonNegative(volume, "Volume");
        this.volume = volume;
    }

    public int getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(int sugarLevel) {
        InputValidator.requireSugarLevel(sugarLevel);
        this.sugarLevel = sugarLevel;
    }

    public String getDescription() {
        String priceText = String.format("%.0f AMD", price);
        switch (category) {
            case FOOD:
                return name + " - " + calories + " calories - " + priceText;
            case DRINK:
                return name + " - " + volume + "L - " + priceText;
            case DESSERT:
                return name + " - sugar level " + sugarLevel + "/10 - " + priceText;
            case OTHER:
            default:
                return name + " - " + priceText;
        }
    }

    @Override
    public void applyDiscount(double percent) {
        InputValidator.requireDiscountPercent(percent);
        // Discount logic belongs here so UI and service code do not duplicate price calculations.
        price = price * (100 - percent) / 100;
    }

    public String toStorageString() {
        return String.join(";",
                "MENUITEM",
                String.valueOf(id),
                name,
                String.valueOf(price),
                category.name(),
                String.valueOf(available),
                String.valueOf(calories),
                String.valueOf(volume),
                String.valueOf(sugarLevel));
    }

    public static MenuItem fromStorageString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 9 || !"MENUITEM".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid menu item storage line");
        }
        return new MenuItem(
                Integer.parseInt(parts[1]),
                parts[2],
                Double.parseDouble(parts[3]),
                MenuCategory.valueOf(parts[4]),
                Boolean.parseBoolean(parts[5]),
                Integer.parseInt(parts[6]),
                Double.parseDouble(parts[7]),
                Integer.parseInt(parts[8]));
    }

    @Override
    public String toString() {
        return getDescription() + (available ? " [available]" : " [unavailable]");
    }
}
