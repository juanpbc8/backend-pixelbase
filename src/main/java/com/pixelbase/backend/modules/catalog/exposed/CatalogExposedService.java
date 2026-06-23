package com.pixelbase.backend.modules.catalog.exposed;

import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;

import java.util.List;
import java.util.Optional;

public interface CatalogExposedService {

    /**
     * Recupera la información compartida de un producto mediante su slug.
     */
    Optional<ProductSharedDto> findBySlug(String slug);

    /**
     * Reduce el stock físico de un producto de manera síncrona y transaccional.
     */
    void decrementStock(Long productId, Integer quantity);

    List<ProductSharedDto> findAllByIds(List<Long> ids);
}
