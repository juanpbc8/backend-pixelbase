package com.pixelbase.backend.modules.catalog.dto.response;

import java.time.LocalDateTime;

public record BrandAdminTableResponse(
    Long id,
    String name,
    String slug,
    String logoUrl,
    long productCount,
    LocalDateTime createdAt
) {
}
