import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        OrderFacade facade = new OrderFacade();

        System.out.println("=== 1. Successful order ===");
        String orderId = facade.placeOrder(cart("LAPTOP", 1, "MOUSE", 2), "Alice");
        System.out.println("Result: " + orderId);

        System.out.println("\n=== 2. Payment declined (stock must be released) ===");
        tryPlace(facade, cart("MONITOR", 1), "Bob");

        System.out.println("\n=== 3. Out of stock (earlier reservations must be released) ===");
        tryPlace(facade, cart("MOUSE", 1, "MONITOR", 3), "Alice");

        System.out.println("\n=== 4. Shipping fails (payment and stock must be rolled back) ===");
        tryPlace(facade, cart("MOUSE", 11), "Alice");

        System.out.println("\n=== 5. Cancel order ===");
        facade.cancelOrder(orderId);

        System.out.println("\n=== 6. Cancel unknown order ===");
        try {
            facade.cancelOrder("ORD-999");
        } catch (OrderFacade.OrderFailedException e) {
            System.out.println("Order failed: " + e.getMessage());
        }
    }

    private static void tryPlace(OrderFacade facade, Map<String, Integer> cart, String customer) {
        try {
            facade.placeOrder(cart, customer);
        } catch (OrderFacade.OrderFailedException e) {
            System.out.println("Order failed: " + e.getMessage());
        }
    }

    private static Map<String, Integer> cart(String sku, int quantity) {
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put(sku, quantity);
        return cart;
    }

    private static Map<String, Integer> cart(String sku1, int quantity1, String sku2, int quantity2) {
        Map<String, Integer> cart = cart(sku1, quantity1);
        cart.put(sku2, quantity2);
        return cart;
    }
}