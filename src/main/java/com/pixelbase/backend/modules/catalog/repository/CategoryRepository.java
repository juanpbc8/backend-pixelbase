package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlug(String slug);

    // Para obtener solo categorías principales
    List<CategoryEntity> findByParentIsNull();

    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.subCategories WHERE c.parent IS NULL")
    List<CategoryEntity> findAllWithChildren();

}