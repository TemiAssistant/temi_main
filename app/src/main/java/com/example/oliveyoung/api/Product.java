package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("name")
    private String name;

    @SerializedName("brand")
    private String brand;

    @SerializedName("category")
    private String category;

    @SerializedName("sub_category")
    private String subCategory;

    @SerializedName("zone")
    private String zone;

    @SerializedName("price")
    private int price;

    @SerializedName("original_price")
    private int originalPrice;

    @SerializedName("discount_rate")
    private int discountRate;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("stock")
    private com.example.oliveyoung.api.Stock stock;

    @SerializedName("image_url")
    private String imageUrl;

    public Product(String productId,
                   String name,
                   int price,
                   String brand,
                   String category,
                   String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public String getZone() { return zone; }
    public int getPrice() { return price; }
    public int getOriginalPrice() { return originalPrice; }
    public int getDiscountRate() { return discountRate; }
    public boolean isActive() { return isActive; }
    public com.example.oliveyoung.api.Stock getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
}
