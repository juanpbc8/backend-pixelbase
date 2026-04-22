package com.pixelbase.backend.modules.catalog.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "El SKU es obligatorio para el inventario")
        String sku,

        String description,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a cero")
        BigDecimal price,

        BigDecimal originalPrice,

        @NotNull(message = "El stock inicial es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "La marca es obligatoria")
        Long brandId,

        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        Map<String, Object> specifications, // El JSONB de hardware

        List<ProductImageRequest> images
) {
}