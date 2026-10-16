import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderFacade {

    public static class OrderFailedException extends RuntimeException {
        public OrderFailedException(String message) {
            super(message);
        }

        public OrderFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record Order(String id, String customer, Map<String, Integer> items,
                         double total, String trackingNumber) {
    }

    private final InventoryService inventory;
    private final PaymentProcessor payment;
    private final ShippingService shipping;
    private final NotificationService notifications;
    private final Map<String, Order> orders = new HashMap<>();
    private int nextOrderNumber = 1;

    public OrderFacade() {
        this(new InventoryService(), new PaymentProcessor(),
                new ShippingService(), new NotificationService());
    }

    public OrderFacade(InventoryService inventory, PaymentProcessor payment,
                       ShippingService shipping, NotificationService notifications) {
        this.inventory = inventory;
        this.payment = payment;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    public String placeOrder(Map<String, Integer> cart, String customer) {
        if (cart.isEmpty()) {
            throw new OrderFailedException("Cart is empty");
        }

        reserveStock(cart);
        double total = calculateTotal(cart);

        if (!payment.charge(customer, total)) {
            releaseStock(cart);
            throw new OrderFailedException("Payment declined");
        }

        String trackingNumber;
        try {
            trackingNumber = shipping.schedule(customer, countItems(cart));
        } catch (IllegalArgumentException e) {
            payment.refund(customer, total);
            releaseStock(cart);
            throw new OrderFailedException("Shipping failed: " + e.getMessage(), e);
        }

        String orderId = "ORD-" + nextOrderNumber++;
        orders.put(orderId, new Order(orderId, customer, Map.copyOf(cart), total, trackingNumber));
        notifications.send(customer, "Order " + orderId + " confirmed. Tracking: " + trackingNumber);
        return orderId;
    }

    public void cancelOrder(String orderId) {
        Order order = orders.remove(orderId);
        if (order == null) {
            throw new OrderFailedException("Unknown order: " + orderId);
        }
        shipping.cancel(order.trackingNumber());
        payment.refund(order.customer(), order.total());
        releaseStock(order.items());
        notifications.send(order.customer(), "Order " + orderId + " cancelled. Refund issued.");
    }

    private void reserveStock(Map<String, Integer> items) {
        Map<String, Integer> reserved = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> item : items.entrySet()) {
            if (!inventory.reserve(item.getKey(), item.getValue())) {
                releaseStock(reserved);
                throw new OrderFailedException("Not enough stock for " + item.getKey());
            }
            reserved.put(item.getKey(), item.getValue());
        }
    }

    private void releaseStock(Map<String, Integer> items) {
        items.forEach(inventory::release);
    }

    private double calculateTotal(Map<String, Integer> items) {
        double total = 0;
        for (Map.Entry<String, Integer> item : items.entrySet()) {
            total += inventory.priceOf(item.getKey()) * item.getValue();
        }
        return total;
    }

    private int countItems(Map<String, Integer> items) {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }
}