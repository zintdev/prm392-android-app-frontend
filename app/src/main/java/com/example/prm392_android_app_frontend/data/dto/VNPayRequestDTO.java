package com.example.prm392_android_app_frontend.data.dto;

public class VNPayRequestDTO {
    private int paymentId;
    private double amount;
    private String orderDescription;

    public VNPayRequestDTO(int paymentId, double amount, String orderDescription) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.orderDescription = orderDescription;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }
}
