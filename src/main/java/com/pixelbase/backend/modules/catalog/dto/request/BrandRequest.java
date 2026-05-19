package com.pixelbase.backend.modules.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Size(max = 100, message = "El nombre de la marca no puede superar los 100 caracteres")
    String name,

    @Size(max = 500, message = "La URL del logo es demasiado larga")
    String logoUrl
) {
}
