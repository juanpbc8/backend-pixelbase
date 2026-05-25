package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>,
    JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySlug(String slug);

    Optional<ProductEntity> findByNameIgnoreCase(String name);

    Optional<ProductEntity> findByPartNumber(String partNumber);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
        "brand",
        "category"
    })
    Page<ProductEntity> findAll(Specification<ProductEntity> spec, @NonNull Pageable pageable);

    @Query(value = "SELECT nextval('product_sku_seq')", nativeQuery = true)
    Long getNextSkuSequence();
}
