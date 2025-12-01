package com.example.oliveyoung;

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
        if (quantity > 0) {
            quantity--;
        }
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public long getLineTotal() {
        return product.getPrice() * quantity;
    }
}
