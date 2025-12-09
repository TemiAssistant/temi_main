package com.example.oliveyoung.api;

public class AiChatRequest {
    private String customer_id;
    private int limit;
    private String query;

    public AiChatRequest(String customer_id, int limit, String query) {
        this.customer_id = customer_id;
        this.limit = limit;
        this.query = query;
    }

    // 필요하면 getter 추가
}