package com.pixelbase.backend.modules.catalog.service.impl;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.exposed.CatalogExposedService;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.catalog.mapper.ProductMapper;
import com.pixelbase.backend.modules.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogExposedServiceImpl implements CatalogExposedService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<ProductSharedDto> findBySlug(String slug) {
        return productRepository.findBySlug(slug)
            .map(productMapper::toSharedDto);
    }

    @Override
    @Transactional
    public void decrementStock(Long productId, Integer quantity) {
        ProductEntity productEntity = productRepository.findById(productId)
            .orElseThrow(() -> new ConflictException("Producto no encontrado con ID: " + productId));
        productEntity.setStock(productEntity.getStock() - quantity);
    }
}
