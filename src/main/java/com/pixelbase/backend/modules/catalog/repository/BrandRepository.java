package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    Optional<BrandEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}