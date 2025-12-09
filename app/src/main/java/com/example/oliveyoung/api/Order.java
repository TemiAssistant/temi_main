// Order.java
package com.example.oliveyoung.api;

public class Order {
    private String order_id;
    private String customer_id;
    private String customer_name;
    private int total_amount;
    private int final_amount;
    private String payment_status; // READY, DONE, ...
    private String order_status;   // 결제대기, 결제완료, ...
    private String created_at;
    private String paid_at;

    public String getOrder_id() { return order_id; }
    public String getCustomer_id() { return customer_id; }
    public String getCustomer_name() { return customer_name; }
    public int getTotal_amount() { return total_amount; }
    public int getFinal_amount() { return final_amount; }
    public String getPayment_status() { return payment_status; }
    public String getOrder_status() { return order_status; }
    public String getCreated_at() { return created_at; }
    public String getPaid_at() { return paid_at; }
}
