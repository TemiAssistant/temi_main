package com.example.oliveyoung.api;

public class AiChatRecommendation {
    private String product_id;
    private String name;
    private String brand;
    private int price;
    private String category;
    private String description;
    private double similarity_score;
    private String reason;

    public String getProduct_id() { return product_id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getSimilarity_score() { return similarity_score; }
    public String getReason() { return reason; }
}
