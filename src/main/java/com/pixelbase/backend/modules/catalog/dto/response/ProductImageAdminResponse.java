package com.pixelbase.backend.modules.catalog.dto.response;

public record ProductImageAdminResponse(
    Long id,
    String url,
    String altText,
    Integer position,
    String publicId
) {
}
