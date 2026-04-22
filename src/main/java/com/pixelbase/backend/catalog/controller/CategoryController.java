package com.pixelbase.backend.catalog.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixelbase.backend.catalog.dto.CategoryRequest;
import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryEntity> list() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CategoryEntity get(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CategoryEntity> create(@Valid @RequestBody CategoryRequest req) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(req.getName());
        if (req.getParentCategoryId() != null) {
            CategoryEntity parent = new CategoryEntity();
            parent.setId(req.getParentCategoryId());
            entity.setParentCategory(parent);
        }
        CategoryEntity saved = categoryService.create(entity);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryEntity> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(req.getName());
        if (req.getParentCategoryId() != null) {
            CategoryEntity parent = new CategoryEntity();
            parent.setId(req.getParentCategoryId());
            entity.setParentCategory(parent);
        }
        CategoryEntity updated = categoryService.update(id, entity);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
