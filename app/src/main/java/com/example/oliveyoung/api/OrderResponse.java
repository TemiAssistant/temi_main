// OrderResponse.java
package com.example.oliveyoung.api;

public class OrderResponse {
    private boolean success;
    private Order order;

    public boolean isSuccess() { return success; }
    public Order getOrder() { return order; }
}
