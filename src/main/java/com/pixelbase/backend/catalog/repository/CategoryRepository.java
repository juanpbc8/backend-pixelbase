package com.pixelbase.backend.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pixelbase.backend.catalog.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findByParentCategoryIsNull();

    List<CategoryEntity> findByParentCategoryId(Long parentCategoryId);

    Optional<CategoryEntity> findByName(String name);

    Boolean existsByName(String name);

}
