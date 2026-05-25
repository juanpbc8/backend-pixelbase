package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductDetailResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IProductService {
    // Storefront
    ProductDetailResponse getBySlug(String slug);

    // El "Endpoint Poderoso": Búsqueda y filtrado dinámico paginado
    PageResponse<ProductCardResponse> getStorefrontProducts(
        String search, Long categoryId, Long brandId,
        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable
    );

    // Admin
    PageResponse<ProductAdminTableResponse> getAdminProducts(
        String search, Long categoryId, Long brandId, Pageable pageable
    );

    ProductAdminDetailResponse getAdminById(Long id);

    ProductAdminDetailResponse create(ProductRequest request);

    ProductAdminDetailResponse update(Long id, ProductRequest request);

    void updateStatus(Long id, ProductStatusRequest request);
}
