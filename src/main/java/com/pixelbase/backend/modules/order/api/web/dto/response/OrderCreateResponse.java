package com.pixelbase.backend.modules.order.api.web.dto.response;

import com.pixelbase.backend.modules.order.domain.OrderStatus;

import java.math.BigDecimal;

public record OrderCreateResponse(
    String orderCode,
    OrderStatus status,
    BigDecimal totalPrice,

    // --- Bloque Agnóstico de Pagos (Evita acoplamiento a MercadoPago) ---
    String paymentSessionToken, // Reemplaza al 'preference_id' de MercadoPago o 'client_secret' de Stripe
    String paymentUrl          // Reemplaza al 'init_point' de MercadoPago por si Angular prefiere redirección
) {
}
