package com.pixelbase.backend.modules.order.infrastructure.payment;

import com.pixelbase.backend.modules.order.infrastructure.payment.dto.PaymentSessionResult;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    PaymentSessionResult createSession(String orderCode, BigDecimal total);
}
