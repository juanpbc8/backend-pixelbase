package com.pixelbase.backend.modules.catalog.dto.response;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductDetailResponse(
    String slug,
    String name,
    String sku,
    String description,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer stock,
    ProductStatus status,
    String partNumber,
    Map<String, Object> specifications,
    BrandSummaryResponse brand,
    CategorySummaryResponse category,
    List<ProductImageResponse> images
) {
}
