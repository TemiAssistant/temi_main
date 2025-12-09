// PaymentInitiateResponse.java
package com.example.oliveyoung.api;

import java.util.Date;

public class PaymentInitiateResponse {
    private boolean success;
    private String payment_key;
    private String order_id;
    private int amount;
    private String order_name;
    private String customer_name;
    private String qr_data;
    private String checkout_url;
    private String created_at; // ISO 문자열로 올 가능성 큼

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
