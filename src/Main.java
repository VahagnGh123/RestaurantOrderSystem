import repository.FileRestaurantRepository;
import repository.RestaurantRepository;
import service.RestaurantManager;
import ui.cli.RestaurantCLI;
import ui.gui.RestaurantGUI;

public class Main {
    public static void main(String[] args) {
        RestaurantRepository repository = new FileRestaurantRepository(
                "data/menu.txt",
                "data/orders.txt",
                "data/tables.txt",
                "data/reservations.txt");

        RestaurantManager manager = new RestaurantManager(repository);
        manager.loadData();

        if (args.length > 0 && "gui".equalsIgnoreCase(args[0])) {
            new RestaurantGUI(manager).show();
        } else {
            new RestaurantCLI(manager).start();
        }
    }
}
