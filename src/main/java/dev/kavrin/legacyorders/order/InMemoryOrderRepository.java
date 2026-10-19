package dev.kavrin.legacyorders.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderRepository {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentNavigableMap<Long, Order> orders = new ConcurrentSkipListMap<>();

    public Order create(List<OrderLine> lines) {
        Order order = new Order(sequence.incrementAndGet(), lines);
        orders.put(order.getId(), order);
        return order;
    }

    public Optional<Order> find(long id) { return Optional.ofNullable(orders.get(id)); }
    public List<Order> findAll() { return new ArrayList<>(orders.values()); }
}
