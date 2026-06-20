package com.pixelbase.backend.modules.user.exposed.dto;

public record UserAuthDto(
    Long id,
    String email,
    String passwordHash,
    String role,
    boolean enabled
) {
}
