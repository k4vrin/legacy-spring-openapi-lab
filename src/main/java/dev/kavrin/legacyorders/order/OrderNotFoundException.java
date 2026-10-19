package dev.kavrin.legacyorders.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException() { super("Order not found"); }
}
