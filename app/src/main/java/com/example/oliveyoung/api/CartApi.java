package com.example.oliveyoung.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CartApi {

    @GET("/api/cart/{owner_id}")
    Call<CartResponse> getCart(@Path("owner_id") String ownerId);

    @DELETE("/api/cart/{owner_id}")
    Call<CartResponse> clearCart(@Path("owner_id") String ownerId);

}
