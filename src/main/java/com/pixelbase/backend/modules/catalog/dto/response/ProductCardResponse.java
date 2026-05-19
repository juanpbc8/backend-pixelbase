package com.pixelbase.backend.modules.catalog.dto.response;

import java.math.BigDecimal;

public record ProductCardResponse(
    String slug,
    String name,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer stock,
    String brandName,
    String mainImageUrl
) {
}
