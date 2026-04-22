package com.pixelbase.backend.modules.catalog.dto.request;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        ProductStatus status
) {
}