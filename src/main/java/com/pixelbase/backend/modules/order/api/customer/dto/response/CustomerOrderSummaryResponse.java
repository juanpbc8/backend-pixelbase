package com.pixelbase.backend.modules.order.api.customer.dto.response;

import com.pixelbase.backend.modules.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Resumen de orden para el historial paginado del cliente")
public record CustomerOrderSummaryResponse(
    @Schema(example = "ORD-20260622-0008")
    String orderCode,

    @Schema(example = "2026-06-22T14:30:00")
    Instant createdAt,

    @Schema(example = "CONFIRMADO")
    OrderStatus status,

    @Schema(example = "1250.50")
    BigDecimal totalPrice,

    @Schema(example = "3")
    int totalItems
) {
}
