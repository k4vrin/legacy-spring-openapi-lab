package dev.example.legacyorders.api;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateOrderRequest {
    @NotNull
    @Size(min = 1)
    private List<@NotNull @Valid OrderLineRequest> lines;

    public List<OrderLineRequest> getLines() { return lines; }
    public void setLines(List<OrderLineRequest> lines) { this.lines = lines; }
}
