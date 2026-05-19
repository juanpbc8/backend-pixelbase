package com.pixelbase.backend.modules.catalog.dto.response;

import java.time.LocalDateTime;

public record CategoryAdminTableResponse(
    Long id,
    String name,
    String slug,
    String parentName,
    Integer level,     // Campo calculado con base en la jerarquía (1, 2 o 3)
    Long productCount, // Super útil para saber cuántos productos quedarían huérfanos si se altera
    LocalDateTime createdAt
) {
}
