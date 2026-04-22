package com.pixelbase.backend.catalog.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.repository.CategoryRepository;
import com.pixelbase.backend.catalog.service.CategoryService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryEntity> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public CategoryEntity findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    @Override
    public CategoryEntity create(CategoryEntity category) {
        return categoryRepository.save(category);
    }

    @Override
    public CategoryEntity update(Long id, CategoryEntity category) {
        CategoryEntity existing = findById(id);
        existing.setName(category.getName());
        existing.setParentCategory(category.getParentCategory());
        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        CategoryEntity existing = findById(id);
        categoryRepository.delete(existing);
    }

}
