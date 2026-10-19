package dev.kavrin.legacyorders.order;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final InMemoryOrderRepository repository;

    public OrderService(InMemoryOrderRepository repository) { this.repository = repository; }
    public Order create(List<OrderLine> lines) { return repository.create(lines); }
    public Order get(long id) { return repository.find(id).orElseThrow(OrderNotFoundException::new); }
    public List<Order> list() { return repository.findAll(); }
}
