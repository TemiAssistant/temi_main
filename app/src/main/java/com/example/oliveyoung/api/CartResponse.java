// CartResponse.java
package com.example.oliveyoung.api;

import java.util.List;

public class CartResponse {
    private boolean success;
    private String device_id;
    private int total_amount;
    private java.util.List<RemoteCartItem> items;

    public boolean isSuccess() { return success; }
    public String getDevice_id() { return device_id; }
    public int getTotal_amount() { return total_amount; }
    public List<RemoteCartItem> getItems() { return items; }
}
