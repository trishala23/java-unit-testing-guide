package com.example.guide.order;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentResult charge(String customerEmail, BigDecimal amount);

    record PaymentResult(boolean success, String transactionId) {
    }
}
