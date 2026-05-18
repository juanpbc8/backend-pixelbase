package com.pixelbase.backend.modules.catalog.dto.response;

public record CategorySummaryResponse(
    Long id,
    String name,
    String slug
) {
}
