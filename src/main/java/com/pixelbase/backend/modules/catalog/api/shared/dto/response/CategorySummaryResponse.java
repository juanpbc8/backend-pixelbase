package com.pixelbase.backend.modules.catalog.api.shared.dto.response;

public record CategorySummaryResponse(
    Long id,
    String name,
    String slug
) {
}
