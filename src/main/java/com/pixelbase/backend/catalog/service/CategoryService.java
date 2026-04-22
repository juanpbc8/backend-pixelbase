package com.pixelbase.backend.catalog.service;

import java.util.List;

import com.pixelbase.backend.catalog.entity.CategoryEntity;

public interface CategoryService {

    List<CategoryEntity> findAll();

    CategoryEntity findById(Long id);

    CategoryEntity create(CategoryEntity category);

    CategoryEntity update(Long id, CategoryEntity category);

    void delete(Long id);

}
