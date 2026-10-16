import java.util.HashSet;
import java.util.Set;

public class ShippingService {

    private static final int MAX_ITEMS_PER_PARCEL = 10;

    private final Set<String> activeShipments = new HashSet<>();
    private int nextNumber = 1000;

    public String schedule(String customer, int itemCount) {
        if (itemCount > MAX_ITEMS_PER_PARCEL) {
            throw new IllegalArgumentException(
                    "Parcel too large: " + itemCount + " items (max " + MAX_ITEMS_PER_PARCEL + ")");
        }
        String trackingNumber = "TRK-" + nextNumber++;
        activeShipments.add(trackingNumber);
        System.out.println("[Shipping] Scheduled " + trackingNumber + " for " + customer);
        return trackingNumber;
    }

    public boolean cancel(String trackingNumber) {
        boolean cancelled = activeShipments.remove(trackingNumber);
        System.out.println("[Shipping] Cancel " + trackingNumber + ": " + (cancelled ? "done" : "not found"));
        return cancelled;
    }
}
