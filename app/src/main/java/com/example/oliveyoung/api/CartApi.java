package com.example.oliveyoung.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CartApi {

    // 예: GET /api/cart/current  혹은 /api/cart
    @GET("/api/cart/current")   // 백엔드 실제 path에 맞춰서
    Call<CartResponse> getCurrentCart();
}
