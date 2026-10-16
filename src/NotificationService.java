public class NotificationService {

    public void send(String customer, String message) {
        System.out.println("[Notification] To " + customer + ": " + message);
    }
}