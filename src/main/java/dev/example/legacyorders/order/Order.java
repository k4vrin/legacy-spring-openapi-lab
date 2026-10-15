package dev.example.legacyorders.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {
    private final long id;
    private final List<OrderLine> lines;

    public Order(long id, List<OrderLine> lines) {
        this.id = id;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public long getId() { return id; }
    public List<OrderLine> getLines() { return lines; }
}
