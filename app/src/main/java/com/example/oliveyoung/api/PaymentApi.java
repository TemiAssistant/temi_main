// PaymentApi.java
package com.example.oliveyoung.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApi {

    @POST("/api/payments/initiate")
    Call<PaymentInitiateResponse> initiatePayment(@Body PaymentInitiateRequest request);

    @GET("/api/payments/orders/{orderId}")
    Call<OrderResponse> getOrder(@Path("orderId") String orderId);
}
