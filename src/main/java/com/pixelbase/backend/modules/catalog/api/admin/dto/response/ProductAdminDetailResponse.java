package com.pixelbase.backend.modules.catalog.api.admin.dto.response;

import com.pixelbase.backend.common.dto.AuditResponse;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.BrandSummaryResponse;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.CategorySummaryResponse;
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
    List<ProductImageAdminResponse> images,
    AuditResponse audit
) {
    public record ProductImageAdminResponse(
        Long id,
        String url,
        String altText,
        Integer position,
        String publicId
    ) {
    }
}
