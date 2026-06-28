package com.pixelbase.backend.modules.catalog.api.admin.dto.response;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductAdminTableResponse(
    Long id,
    String sku,
    String name,
    BigDecimal price,
    Integer stock,
    ProductStatus status,
    String brandName,
    String categoryName,
    Instant updatedAt,
    String updatedBy
) {
}
