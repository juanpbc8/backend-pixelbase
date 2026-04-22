package com.pixelbase.backend.modules.catalog.dto.response;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Respuesta COMPLETA (Principalmente para el Admin y Detalle de Producto)
public record ProductResponse(
        Long id,
        String name,
        String sku,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        ProductStatus status,
        Map<String, Object> specifications,
        BrandResponse brand,
        CategoryResponse category,
        List<ProductImageResponse> images
) {
}