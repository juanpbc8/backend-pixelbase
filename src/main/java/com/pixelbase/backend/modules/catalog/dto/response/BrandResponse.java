package com.pixelbase.backend.modules.catalog.dto.response;

public record BrandResponse(
        Long id,
        String name,
        String slug,
        String logoUrl
) {
}