package com.pixelbase.backend.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductImageRequest(
        @NotBlank(message = "La URL de la imagen es obligatoria")
        String url,
        String altText,
        Integer position
) {
}