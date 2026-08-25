package com.example.guide.order;

public interface NotificationService {

    void sendOrderConfirmation(String email, String orderId);

    void sendOrderFailure(String email, String reason);
}
