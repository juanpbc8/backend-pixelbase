package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    // Repo ayuda para limpiezas específicas.
}