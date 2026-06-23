package com.pixelbase.backend.modules.catalog.exposed.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO inmutable compartido para transferir la información inmutable del producto
 * entre el catálogo y las órdenes sin cruzar entidades JPA.
 */
public record ProductSharedDto(
    Long id,
    String slug,
    String name,
    String sku,
    String partNumber,
    BigDecimal price,
    Integer stock,
    boolean active,
    List<ProductImageSharedDto> images // Incluye tus imágenes multimedia
) {
    public record ProductImageSharedDto(
        Long id,
        String url,
        boolean isMain
    ) {
    }
}
