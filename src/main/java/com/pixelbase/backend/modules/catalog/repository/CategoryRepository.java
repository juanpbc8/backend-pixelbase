package com.pixelbase.backend.modules.catalog.repository;

import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlug(String slug);

    @Query("""
        SELECT c FROM CategoryEntity c
        LEFT JOIN FETCH c.subCategories
        WHERE c.slug = :slug
        """)
    Optional<CategoryEntity> findBySlugWithChildren(@Param("slug") String slug);

    @Query("""
        SELECT DISTINCT c FROM CategoryEntity c
        LEFT JOIN FETCH c.subCategories
        WHERE c.parent IS NULL
        ORDER BY c.name ASC
        """)
    List<CategoryEntity> findAllWithChildren();

    boolean existsByParentId(Long parentId);

    long countByParentId(Long parentId);

    Optional<CategoryEntity> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p WHERE p.category.id = :categoryId")
    boolean hasProducts(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
        SELECT new com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse(
            c.id,
            c.name,
            c.slug,
            parent.name,
            CASE
                WHEN parent.id IS NULL THEN 1
                WHEN parent.parent.id IS NULL THEN 2
                ELSE 3
            END,
            COUNT(p.id),
            c.createdAt
        )
        FROM CategoryEntity c
        LEFT JOIN c.parent parent
        LEFT JOIN ProductEntity p ON p.category.id = c.id
        GROUP BY c.id, c.name, c.slug, parent.name, parent.id, parent.parent.id, c.createdAt
        ORDER BY c.name ASC
        """)
    List<CategoryAdminTableResponse> findAdminTable();
}
