import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Double> prices = new HashMap<>();

    public InventoryService() {
        addProduct("LAPTOP", 5, 1000.00);
        addProduct("MOUSE", 20, 25.00);
        addProduct("MONITOR", 2, 300.00);
    }

    public void addProduct(String sku, int quantity, double price) {
        stock.put(sku, quantity);
        prices.put(sku, price);
    }

    public boolean reserve(String sku, int quantity) {
        int available = stock.getOrDefault(sku, 0);
        if (quantity <= 0 || available < quantity) {
            System.out.println("[Inventory] Cannot reserve " + quantity + " x " + sku
                    + " (available: " + available + ")");
            return false;
        }
        stock.put(sku, available - quantity);
        System.out.println("[Inventory] Reserved " + quantity + " x " + sku);
        return true;
    }

    public void release(String sku, int quantity) {
        stock.merge(sku, quantity, Integer::sum);
        System.out.println("[Inventory] Released " + quantity + " x " + sku);
    }

    public double priceOf(String sku) {
        Double price = prices.get(sku);
        if (price == null) {
            throw new IllegalArgumentException("Unknown product: " + sku);
        }
        return price;
    }

    public int availableQuantity(String sku) {
        return stock.getOrDefault(sku, 0);
    }
}
