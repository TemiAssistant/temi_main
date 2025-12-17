package com.example.oliveyoung.api;

import com.google.gson.annotations.SerializedName;

public class PaymentInitiateResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("payment_key")
    private String payment_key;

    @SerializedName("order_id")
    private String order_id;

    @SerializedName("amount")
    private int amount;

    @SerializedName("order_name")
    private String order_name;

    @SerializedName("customer_name")
    private String customer_name;

    @SerializedName("qr_data")
    private String qr_data;

    @SerializedName("checkout_url")
    private String checkout_url;

    @SerializedName("created_at")
    private String created_at;

    public boolean isSuccess() { return success; }
    public String getPayment_key() { return payment_key; }
    public String getOrder_id() { return order_id; }
    public int getAmount() { return amount; }
    public String getOrder_name() { return order_name; }
    public String getCustomer_name() { return customer_name; }
    public String getQr_data() { return qr_data; }
    public String getCheckout_url() { return checkout_url; }
    public String getCreated_at() { return created_at; }
}
