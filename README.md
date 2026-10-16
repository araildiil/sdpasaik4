# Order Processing Facade

Java implementation of the **Facade** structural design pattern.

**Author:** Arailym Amangeldi, group SE-2528

## Overview

Placing an online order involves several independent services: checking stock,
charging the customer, scheduling delivery and sending a confirmation. A client
that talks to all of them directly has to know the correct order of the calls
and has to undo the earlier steps by hand when a later step fails.

`OrderFacade` solves this. It exposes two simple methods and coordinates the
whole subsystem behind them, including rollback on failure.

```text
String orderId = facade.placeOrder(cart, "Alice");
facade.cancelOrder(orderId);
```

## Project structure

```
src/
├── Main.java                 Client, uses only the Facade
├── OrderFacade.java          Facade
├── InventoryService.java     Subsystem: stock and prices
├── PaymentProcessor.java     Subsystem: charge and refund
├── ShippingService.java      Subsystem: schedule and cancel delivery
└── NotificationService.java  Subsystem: customer notifications
```



## How it works

### `placeOrder(cart, customer)`

1. Reserve stock for every item in the cart.
2. Calculate the total price using the inventory prices.
3. Charge the customer.
4. Schedule delivery.
5. Save the order and notify the customer.

If a step fails, the facade undoes the previous steps and throws
`OrderFailedException`:

| Failure           | Rollback                               |
| ----------------- | -------------------------------------- |
| Item out of stock | Items reserved so far are released     |
| Payment declined  | All reserved stock is released         |
| Shipping rejected | Payment is refunded, stock is released |

### `cancelOrder(orderId)`

Cancels the shipment, refunds the payment, releases the stock and notifies the
customer. An unknown order id results in `OrderFailedException`.

## What the Facade hides

- the order in which the subsystem classes must be called
- price calculation and parcel size checks
- rollback logic for every failure case
- subsystem-specific results and exceptions (the client only sees
  `OrderFailedException`)

## What stays directly accessible

All four subsystem classes are public and independent. They do not know that
the facade exists (no back-references), so an advanced client can still use
`InventoryService`, `PaymentProcessor`, `ShippingService` or
`NotificationService` directly without the facade.

## Clean Code principles applied

1. **Meaningful names**: `placeOrder` and `cancelOrder` describe the intent.
2. **Single Responsibility**: the facade coordinates, the subsystem classes contain the logic.
3. **No leaking of subsystem details**: the public API exposes only `OrderFailedException`.
4. **Small, focused classes**: each subsystem class has one responsibility.
5. **Proper error handling**: every failed step triggers a compensating action.
6. **Small private methods and constructor injection**: `reserveStock`, `releaseStock`, `calculateTotal`, and an `OrderFacade` constructor that accepts custom subsystem objects.


## Build and run

```bash
mkdir out
javac -d out src/*.java
java -cp out Main
```

## Demo scenarios

`Main` runs six scenarios:

1. Successful order
2. Payment declined, stock released
3. Out of stock, earlier reservations released
4. Shipping rejected (parcel over 10 items), payment refunded and stock released
5. Cancel an order
6. Cancel an unknown order

### Expected output

```
=== 1. Successful order ===
[Inventory] Reserved 1 x LAPTOP
[Inventory] Reserved 2 x MOUSE
[Payment] Charged 1050.00 to Alice
[Shipping] Scheduled TRK-1000 for Alice
[Notification] To Alice: Order ORD-1 confirmed. Tracking: TRK-1000
Result: ORD-1

=== 2. Payment declined (stock must be released) ===
[Inventory] Reserved 1 x MONITOR
[Payment] Declined: Bob has 50.00, needs 300.00
[Inventory] Released 1 x MONITOR
Order failed: Payment declined

=== 3. Out of stock (earlier reservations must be released) ===
[Inventory] Reserved 1 x MOUSE
[Inventory] Cannot reserve 3 x MONITOR (available: 2)
[Inventory] Released 1 x MOUSE
Order failed: Not enough stock for MONITOR

=== 4. Shipping fails (payment and stock must be rolled back) ===
[Inventory] Reserved 11 x MOUSE
[Payment] Charged 275.00 to Alice
[Payment] Refunded 275.00 to Alice
[Inventory] Released 11 x MOUSE
Order failed: Shipping failed: Parcel too large: 11 items (max 10)

=== 5. Cancel order ===
[Shipping] Cancel TRK-1000: done
[Payment] Refunded 1050.00 to Alice
[Inventory] Released 1 x LAPTOP
[Inventory] Released 2 x MOUSE
[Notification] To Alice: Order ORD-1 cancelled. Refund issued.

=== 6. Cancel unknown order ===
Order failed: Unknown order: ORD-999
```

## Notes

The subsystem is simulated: stock, balances and shipments are kept in memory
and every service prints what it does to the console.