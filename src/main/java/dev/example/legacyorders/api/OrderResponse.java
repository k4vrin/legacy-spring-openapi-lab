package dev.example.legacyorders.api;

import dev.example.legacyorders.order.Order;
import dev.example.legacyorders.order.OrderLine;
import java.util.List;

public final class OrderResponse {
    private final Order order;

    public OrderResponse(Order order) { this.order = order; }
    public long getId() { return order.getId(); }
    public List<OrderLine> getLines() { return order.getLines(); }
}
