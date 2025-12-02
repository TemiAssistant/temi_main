package com.example.oliveyoung.ui.checkout;

import com.example.oliveyoung.api.Product;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product) {
        this.product = product;
        this.quantity = 1;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increase() {
        quantity++;
    }

    public void decrease() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public long getLineTotal() {
        return (long) product.getPrice() * quantity;
    }
}
