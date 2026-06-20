package com.pixelbase.backend.modules.order.infrastructure.payment.dto;

public record PaymentSessionResult(
    String sessionToken,
    String url
) {
}
