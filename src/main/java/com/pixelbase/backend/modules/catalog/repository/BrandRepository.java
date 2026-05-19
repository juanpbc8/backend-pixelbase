package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import com.pixelbase.backend.modules.catalog.dto.response.BrandAdminTableResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    List<BrandEntity> findAllByOrderByNameAsc();

    Optional<BrandEntity> findBySlug(String slug);

    Optional<BrandEntity> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.brand.id = :brandId")
    long countProductsByBrandId(@Param("brandId") Long brandId);

    // TRUCO SENIOR: Consulta de agregación masiva en un solo viaje de red (Evita N+1)
    @Query("""
        SELECT new com.pixelbase.backend.modules.catalog.dto.response.BrandAdminTableResponse(
            b.id,
            b.name,
            b.slug,
            b.logoUrl,
            COUNT(p)
        )
        FROM BrandEntity b
        LEFT JOIN ProductEntity p ON p.brand = b
        GROUP BY b.id, b.name, b.slug, b.logoUrl
        ORDER BY b.name ASC
        """)
    List<BrandAdminTableResponse> findAllAdminTable();
}
