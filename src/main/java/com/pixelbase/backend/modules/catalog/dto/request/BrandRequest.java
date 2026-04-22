package com.pixelbase.backend.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        String name,
        String logoUrl
) {
}