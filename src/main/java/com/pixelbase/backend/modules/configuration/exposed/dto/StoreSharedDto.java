package com.pixelbase.backend.modules.configuration.exposed.dto;

public record StoreSharedDto(
    Long id,
    String addressLine,
    String department,
    String province,
    String district,
    boolean active
) {
}
