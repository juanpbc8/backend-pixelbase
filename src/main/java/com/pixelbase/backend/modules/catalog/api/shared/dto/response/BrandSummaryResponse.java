package com.pixelbase.backend.modules.catalog.api.shared.dto.response;

public record BrandSummaryResponse(
    Long id,
    String name,
    String slug
) {
}
