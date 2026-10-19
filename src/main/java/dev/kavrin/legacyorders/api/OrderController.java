package dev.kavrin.legacyorders.api;

import dev.kavrin.legacyorders.order.OrderLine;
import dev.kavrin.legacyorders.order.OrderService;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/orders", produces = "application/json")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderLine> lines = request.getLines().stream()
                .map(line -> new OrderLine(line.getProductCode(), line.getQuantity()))
                .collect(Collectors.toList());
        OrderResponse response = new OrderResponse(service.create(lines));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable long id) { return new OrderResponse(service.get(id)); }

    @GetMapping
    public List<OrderResponse> list() {
        return service.list().stream().map(OrderResponse::new).collect(Collectors.toList());
    }
}
