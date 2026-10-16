import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {

    private final Map<String, Double> balances = new HashMap<>();

    public PaymentProcessor() {
        openAccount("Alice", 2000.00);
        openAccount("Bob", 50.00);
    }

    public void openAccount(String customer, double balance) {
        balances.put(customer, balance);
    }

    public boolean charge(String customer, double amount) {
        double balance = balances.getOrDefault(customer, 0.0);
        if (balance < amount) {
            System.out.printf("[Payment] Declined: %s has %.2f, needs %.2f%n", customer, balance, amount);
            return false;
        }
        balances.put(customer, balance - amount);
        System.out.printf("[Payment] Charged %.2f to %s%n", amount, customer);
        return true;
    }

    public void refund(String customer, double amount) {
        balances.merge(customer, amount, Double::sum);
        System.out.printf("[Payment] Refunded %.2f to %s%n", amount, customer);
    }

    public double balanceOf(String customer) {
        return balances.getOrDefault(customer, 0.0);
    }
}
