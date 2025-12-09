package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;

public class RemoteCartItem {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private int price;

    @SerializedName("quantity")
    private int quantity;

    // --- getter들 ---
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
