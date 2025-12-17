package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartResponse {

    @SerializedName("success")
    private boolean success;

    // 백엔드가 CartResponse(success=True, cart=cart)로 반환하므로 cart 래핑 필수
    @SerializedName("cart")
    private Cart cart;

    public boolean isSuccess() { return success; }
    public Cart getCart() { return cart; }

    public static class Cart {
        @SerializedName("items")
        private List<RemoteCartItem> items;

        @SerializedName("total_amount")
        private int totalAmount;

        public List<RemoteCartItem> getItems() { return items; }
        public int getTotalAmount() { return totalAmount; }
    }
}
