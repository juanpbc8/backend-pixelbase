package com.pixelbase.backend.modules.security.api.web.dto.response;

public record AuthResponse(
    String token,
    String email,
    String role
) {
}
