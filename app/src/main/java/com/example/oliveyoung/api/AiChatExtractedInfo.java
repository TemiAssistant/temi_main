package com.example.oliveyoung.api;

import java.util.List;

public class AiChatExtractedInfo {
    private String skin_type;
    private String category;
    private AiChatPriceRange price_range;
    private List<String> keywords;

    public String getSkin_type() { return skin_type; }
    public String getCategory() { return category; }
    public AiChatPriceRange getPrice_range() { return price_range; }
    public List<String> getKeywords() { return keywords; }
}
