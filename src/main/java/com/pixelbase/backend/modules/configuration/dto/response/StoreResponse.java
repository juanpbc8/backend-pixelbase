package com.pixelbase.backend.modules.configuration.dto.response;

public record StoreResponse(
    Long id,
    String name,
    String addressLine,
    String department,
    String province,
    String district
) {
}
