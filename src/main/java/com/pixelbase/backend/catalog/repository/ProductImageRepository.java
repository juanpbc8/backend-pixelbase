package com.pixelbase.backend.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pixelbase.backend.catalog.entity.ProductImageEntity;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    List<ProductImageEntity> findByProductIdOrderByPositionAsc(Long productId);
    
}
