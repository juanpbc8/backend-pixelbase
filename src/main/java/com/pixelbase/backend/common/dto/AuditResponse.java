package com.pixelbase.backend.common.dto;

import java.time.Instant;

public record AuditResponse(
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {
}
