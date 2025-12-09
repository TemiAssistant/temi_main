// CartApi.java
package com.example.oliveyoung.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CartApi {

    // GET /api/cart/current?device_id=temi-001
    @GET("/api/cart/current")
    Call<CartResponse> getCurrentCart(@Query("device_id") String deviceId);
}
