package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>,
    JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySlug(String slug);

    boolean existsByPartNumber(String partNumber);

    boolean existsBySlug(String slug);

    @Query(value = "SELECT nextval('product_sku_seq')", nativeQuery = true)
    Long getNextSkuSequence();
}
