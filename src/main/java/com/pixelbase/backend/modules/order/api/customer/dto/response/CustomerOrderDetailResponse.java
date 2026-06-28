package com.pixelbase.backend.modules.order.api.customer.dto.response;

import com.pixelbase.backend.modules.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Detalle exhaustivo de una orden de compra para el Storefront")
public record CustomerOrderDetailResponse(
    String orderCode,
    Instant createdAt,
    OrderStatus status,
    BigDecimal totalPrice,
    String deliveryType,

    CustomerOrderAddressDto address,
    List<CustomerOrderItemResponse> items
) {
    public record CustomerOrderAddressDto(
        String addressLine,
        String department,
        String province,
        String district,
        String reference,
        String contactFirstName,
        String contactLastName,
        String contactPhone
    ) {
    }

    public record CustomerOrderItemResponse(
        String productSlug,
        String productNameSnapshot,
        int quantity,
        BigDecimal priceSnapshot,
        String imageUrl
    ) {
    }
}
