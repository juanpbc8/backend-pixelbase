package com.pixelbase.backend.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        String name,
        Long parentId // Para categorías anidadas
) {
}