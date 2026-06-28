package com.pixelbase.backend.modules.configuration.api.web.dto.response;

public record StoreResponse(
    Long id,
    String name,
    String addressLine,
    String department,
    String province,
    String district
) {
}
