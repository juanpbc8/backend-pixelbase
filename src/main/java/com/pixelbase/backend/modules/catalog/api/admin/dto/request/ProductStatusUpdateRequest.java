package com.pixelbase.backend.modules.catalog.api.admin.dto.request;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
    @NotNull(message = "El estado es obligatorio")
    ProductStatus status
) {
}
