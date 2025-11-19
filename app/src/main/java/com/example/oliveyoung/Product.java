package com.example.oliveyoung;

import java.util.List;
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

    // location과 stock은 map이나 별도 클래스로 뽑을 수도 있지만
    // 여기서는 zone만 먼저 쓰니까 필드 하나만 뽑아둘게
    private Map<String, Object> location;

    // Firestore 역직렬화를 위해 기본 생성자 필수
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

    /**
     * location.zone 값을 편하게 꺼내기 위한 메서드
     */
    public String getLocationZone() {
        if (location == null) return null;
        Object zone = location.get("zone");
        return zone != null ? zone.toString() : null;
    }
}
