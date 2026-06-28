package com.pixelbase.backend.modules.catalog.api.shared.dto.response;

public record BrandResponse(
    Long id,
    String name,
    String slug,
    String logoUrl
) {
}
