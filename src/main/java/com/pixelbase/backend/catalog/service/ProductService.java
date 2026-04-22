package com.pixelbase.backend.catalog.service;

import org.springframework.data.domain.Page;

import com.pixelbase.backend.catalog.entity.ProductEntity;

public interface ProductService {

    Page<ProductEntity> search(String keyword, org.springframework.data.domain.Pageable pageable);

    ProductEntity findById(Long id);

    ProductEntity create(ProductEntity product);

    ProductEntity update(Long id, ProductEntity product);

    void delete(Long id);

}
