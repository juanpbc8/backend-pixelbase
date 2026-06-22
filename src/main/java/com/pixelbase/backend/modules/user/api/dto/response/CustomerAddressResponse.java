package com.pixelbase.backend.modules.user.api.dto.response;

public record CustomerAddressResponse(
    Long id,
    String addressLine,
    String department,
    String province,
    String district,
    String reference,
    boolean isDefault
) {
}
