package com.pixelbase.backend.modules.catalog.dto.response;

public record BrandAdminTableResponse(
    Long id,
    String name,
    String slug,
    String logoUrl,
    long productCount
) {
}
