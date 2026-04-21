package com.pixelbase.backend.modules.security.dto;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}