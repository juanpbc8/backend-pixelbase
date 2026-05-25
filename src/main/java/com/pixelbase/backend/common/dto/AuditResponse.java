package com.pixelbase.backend.common.dto;

import java.time.LocalDateTime;

public record AuditResponse(
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {
}
