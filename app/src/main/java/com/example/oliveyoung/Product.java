package com.example.oliveyoung;

import java.util.Map;

public class Product {

    private String product_id;
    private String name;
    private String brand;
    private String category;
    private String sub_category;
    private long price;
    private long original_price;
    private long discount_rate;
    private Map<String, Object> location;

    public Product() {}

    public String getProduct_id() {
        return product_id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public String getSub_category() {
        return sub_category;
    }

    public long getPrice() {
        return price;
    }

    public long getOriginal_price() {
        return original_price;
    }

    public long getDiscount_rate() {
        return discount_rate;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public String getLocationZone() {
        if (location == null) return null;
        Object zone = location.get("zone");
        return zone != null ? zone.toString() : null;
    }
}