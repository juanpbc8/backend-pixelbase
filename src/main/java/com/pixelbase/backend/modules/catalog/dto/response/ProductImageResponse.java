package com.pixelbase.backend.modules.catalog.dto.response;

public record ProductImageResponse(
        String url,
        String altText,
        Integer position
) {
}