package com.pixelbase.backend.modules.catalog.dto.response;

import com.pixelbase.backend.common.dto.AuditResponse;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductAdminDetailResponse(
    Long id,
    String name,
    String description,
    String partNumber,
    String sku,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer stock,
    ProductStatus status,
    String slug,
    Map<String, Object> specifications,
    BrandSummaryResponse brand,
    CategorySummaryResponse category,
    List<ProductImageResponse> images,
    AuditResponse audit
) {
}
