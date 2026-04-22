package com.pixelbase.backend.catalog.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.entity.ProductEntity;
import com.pixelbase.backend.catalog.repository.CategoryRepository;
import com.pixelbase.backend.catalog.repository.ProductRepository;
import com.pixelbase.backend.catalog.service.ProductService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<ProductEntity> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public ProductEntity findById(Long id) {
        Optional<ProductEntity> opt = productRepository.findDetailedById(id);
        return opt.orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Override
    public ProductEntity create(ProductEntity product) {
        // ensure categories exist and attach
        if (product.getCategories() != null) {
            for (int i = 0; i < product.getCategories().size(); i++) {
                CategoryEntity c = product.getCategories().get(i);
                if (c != null && c.getId() != null) {
                    CategoryEntity found = categoryRepository.findById(c.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + c.getId()));
                    product.getCategories().set(i, found);
                }
            }
        }
        return productRepository.save(product);
    }

    @Override
    public ProductEntity update(Long id, ProductEntity product) {
        ProductEntity existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        if (product.getCategories() != null) {
            existing.getCategories().clear();
            for (CategoryEntity c : product.getCategories()) {
                if (c != null && c.getId() != null) {
                    CategoryEntity found = categoryRepository.findById(c.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + c.getId()));
                    existing.getCategories().add(found);
                }
            }
        }
        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        ProductEntity existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        productRepository.delete(existing);
    }

}
