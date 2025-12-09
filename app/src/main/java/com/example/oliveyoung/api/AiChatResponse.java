package com.example.oliveyoung.api;

import java.util.List;

public class AiChatResponse {
    private boolean success;
    private String query;
    private AiChatExtractedInfo extracted_info;
    private List<AiChatRecommendation> recommendations;
    private int total;
    private String message;

    public boolean isSuccess() { return success; }
    public String getQuery() { return query; }
    public AiChatExtractedInfo getExtracted_info() { return extracted_info; }
    public List<AiChatRecommendation> getRecommendations() { return recommendations; }
    public int getTotal() { return total; }
    public String getMessage() { return message; }
}
