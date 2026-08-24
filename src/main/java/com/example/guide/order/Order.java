package com.example.guide.order;

import java.math.BigDecimal;

public class Order {

    public enum Status { CREATED, PAID, FAILED }

    private String id;
    private final String customerEmail;
    private final String sku;
    private final int quantity;
    private final BigDecimal amount;
    private Status status;

    public Order(String customerEmail, String sku, int quantity, BigDecimal amount) {
        this.customerEmail = customerEmail;
        this.sku = sku;
        this.quantity = quantity;
        this.amount = amount;
        this.status = Status.CREATED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
