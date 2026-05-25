package com.pixelbase.backend.modules.catalog.dto.response;

public record ProductImageResponse(
    Long id,
    String url,
    String altText,
    Integer position
) {
}
