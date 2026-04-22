package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IProductService {
    // Storefront
    ProductResponse getBySlug(String slug);

    // El "Endpoint Poderoso": Búsqueda y filtrado dinámico paginado
    PageResponse<ProductCardResponse> getStorefrontProducts(
            String search, Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable
    );

    // Admin
    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    // Admin: Todos los productos (Active/Inactive), formato completo
    PageResponse<ProductResponse> getAdminProducts(
            String search, Long categoryId, Long brandId, Pageable pageable
    );

    void updateStatus(Long id, ProductStatusRequest request);
}