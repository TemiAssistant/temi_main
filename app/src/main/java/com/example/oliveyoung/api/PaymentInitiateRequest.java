package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaymentInitiateRequest {

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_phone")
    private String customerPhone;

    @SerializedName("items")
    private List<PaymentItem> items;

    @SerializedName("total_amount")
    private int totalAmount;

    @SerializedName("use_points")
    private int usePoints;

    @SerializedName("final_amount")
    private int finalAmount;

    public PaymentInitiateRequest(
            String customerName,
            String customerPhone,
            List<PaymentItem> items,
            int totalAmount,
            int usePoints,
            int finalAmount
    ) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.items = items;
        this.totalAmount = totalAmount;
        this.usePoints = usePoints;
        this.finalAmount = finalAmount;
    }
}
