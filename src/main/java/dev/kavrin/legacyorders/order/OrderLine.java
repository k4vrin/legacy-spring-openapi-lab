package dev.kavrin.legacyorders.order;

public final class OrderLine {
    private final String productCode;
    private final int quantity;

    public OrderLine(String productCode, int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public String getProductCode() { return productCode; }
    public int getQuantity() { return quantity; }
}
