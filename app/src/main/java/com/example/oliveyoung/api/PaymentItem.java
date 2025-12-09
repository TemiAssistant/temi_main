// PaymentItem.java
package com.example.oliveyoung.api;

public class PaymentItem {
    private String product_id;
    private String name;
    private int quantity;
    private int price;
    private int total_price;

    public PaymentItem(String productId, String name, int quantity, int price) {
        this.product_id = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.total_price = price * quantity;
    }

    // 필요하면 getter만 추가
    public String getProduct_id() { return product_id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
    public int getTotal_price() { return total_price; }
}

