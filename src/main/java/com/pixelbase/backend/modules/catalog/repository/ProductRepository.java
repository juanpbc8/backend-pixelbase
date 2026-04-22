package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findBySlug(String slug);

    // Para el Storefront: Solo productos activos
    List<ProductEntity> findByStatus(ProductStatus status);

    boolean existsBySku(String sku);
}