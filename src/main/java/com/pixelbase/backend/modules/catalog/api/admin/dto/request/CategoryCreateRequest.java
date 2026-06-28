package com.pixelbase.backend.modules.catalog.api.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la categoría debe tener entre 3 y 100 caracteres")
    String name,

    Long parentId // Opcional: Si es nulo, la categoría se creará como Nivel 1 (Root)
) {
}
