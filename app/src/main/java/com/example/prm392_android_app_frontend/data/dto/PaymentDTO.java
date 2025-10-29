package com.example.prm392_android_app_frontend.data.dto;

public class PaymentDTO {
    private int orderId;
    private String method; // "VNPAY", "COD", etc.
    private double amount;

    public PaymentDTO(int orderId, String method, double amount) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
