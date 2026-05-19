package com.pixelbase.backend.modules.catalog.dto.response;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductAdminTableResponse(
    Long id,
    String sku,
    String name,
    BigDecimal price,
    Integer stock,
    ProductStatus status,
    String brandName,
    String categoryName,
    LocalDateTime createdAt
) {
}
