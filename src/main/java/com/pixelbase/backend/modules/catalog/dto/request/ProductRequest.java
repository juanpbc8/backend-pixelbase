package com.pixelbase.backend.modules.catalog.dto.request;

import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequest(
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    String name,

    @NotBlank(message = "La descripción es obligatoria")
    String description,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0.")
    BigDecimal price,

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio original debe ser mayor a 0.")
    BigDecimal originalPrice,

    @NotNull(message = "El stock inicial es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stock,

    @NotBlank(message = "El número de parte es obligatorio")
    @Size(max = 100, message = "El Part Number no puede superar los 100 caracteres.")
    String partNumber,

    @NotNull(message = "El estado del producto es obligatorio")
    ProductStatus status,

    @NotNull(message = "La marca asociada es obligatoria")
    Long brandId,

    @NotNull(message = "La categoría asociada es obligatoria")
    Long categoryId,

    Map<String, Object> specifications, // El JSONB de hardware

    @Valid
    List<ProductImageRequest> images
) {
    public record ProductImageRequest(
        @NotBlank(message = "La URL de la imagen es obligatoria.")
        String url,

        @Size(max = 255, message = "El texto alternativo no puede superar los 255 caracteres.")
        String altText,
        
        @NotBlank(message = "El publicId de la imagen es obligatorio para la persistencia de " +
            "infraestructura.")
        String publicId
    ) {
    }
}
