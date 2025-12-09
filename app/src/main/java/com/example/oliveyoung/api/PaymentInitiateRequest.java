// PaymentInitiateRequest.java
package com.example.oliveyoung.api;

import java.util.List;

public class PaymentInitiateRequest {
    private String customer_id;
    private String customer_name;
    private String customer_email;
    private String customer_phone;
    private List<PaymentItem> items;
    private int total_amount;
    private int use_points;
    private int final_amount;

    public PaymentInitiateRequest(String customerId,
                                  String customerName,
                                  String customerEmail,
                                  String customerPhone,
                                  List<PaymentItem> items,
                                  int totalAmount,
                                  int usePoints,
                                  int finalAmount) {
        this.customer_id = customerId;
        this.customer_name = customerName;
        this.customer_email = customerEmail;
        this.customer_phone = customerPhone;
        this.items = items;
        this.total_amount = totalAmount;
        this.use_points = usePoints;
        this.final_amount = finalAmount;
    }
}
