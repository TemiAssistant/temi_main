package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("items")
    private List<RemoteCartItem> items;

    @SerializedName("total_amount")
    private int totalAmount;   // 써도 되고, 안 써도 됨

    public boolean isSuccess() { return success; }
    public List<RemoteCartItem> getItems() { return items; }
    public int getTotalAmount() { return totalAmount; }
}
