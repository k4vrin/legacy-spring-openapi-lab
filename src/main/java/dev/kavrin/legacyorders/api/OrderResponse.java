package dev.kavrin.legacyorders.api;

import dev.kavrin.legacyorders.order.Order;
import dev.kavrin.legacyorders.order.OrderLine;
import java.util.List;

public final class OrderResponse {
    private final Order order;

    public OrderResponse(Order order) { this.order = order; }
    public long getId() { return order.getId(); }
    public List<OrderLine> getLines() { return order.getLines(); }
}
