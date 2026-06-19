package com.pixelbase.backend.modules.catalog.exposed;

import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;

public interface ICatalogService {

    /**
     * Recupera la información compartida de un producto mediante su slug.
     */
    ProductSharedDto getBySlug(String slug);

    /**
     * Reduce el stock físico de un producto de manera síncrona y transaccional.
     */
    void decrementStock(Long productId, Integer quantity);
}
