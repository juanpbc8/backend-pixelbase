package com.pixelbase.backend.modules.catalog.dto.response;

import java.math.BigDecimal;

// Respuesta OPTIMIZADA (Para el Storefront / Listado de productos)
// Aquí quitamos datos pesados como especificaciones completas o stock exacto si no es necesario
public record ProductCardResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        String brandName,
        String mainImageUrl // Solo la primera imagen para la tarjeta
) {
}