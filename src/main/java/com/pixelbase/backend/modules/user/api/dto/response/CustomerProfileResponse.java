package com.pixelbase.backend.modules.user.api.dto.response;

import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.modules.user.domain.Role;

public record CustomerProfileResponse(
    String email,
    String firstName,
    String lastName,
    String phone,
    DocumentType documentType,
    String documentNumber,
    Role role
) {
}
