package com.pixelbase.backend.catalog.service;

import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.entity.ProductEntity;
import com.pixelbase.backend.catalog.repository.CategoryRepository;
import com.pixelbase.backend.catalog.repository.ProductRepository;
import com.pixelbase.backend.catalog.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductEntity sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = ProductEntity.builder()
                .id(1L)
                .name("Laptop Lenovo")
                .description("Ultrabook")
                .categories(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("search sin keyword devuelve página completa")
    void searchWithoutKeywordReturnsAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductEntity> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductEntity> result = productService.search(null, pageable);

        assertThat(result.getContent()).hasSize(1).containsExactly(sampleProduct);
        verify(productRepository).findAll(pageable);
        verify(productRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("search con keyword filtra por nombre")
    void searchWithKeywordFiltersByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductEntity> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findByNameContainingIgnoreCase("lenovo", pageable)).thenReturn(page);

        Page<ProductEntity> result = productService.search("lenovo", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("lenovo", pageable);
    }

    @Test
    @DisplayName("findById devuelve el producto cuando existe")
    void findByIdReturnsProduct() {
        when(productRepository.findDetailedById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductEntity result = productService.findById(1L);

        assertThat(result.getName()).isEqualTo("Laptop Lenovo");
    }

    @Test
    @DisplayName("findById lanza excepción cuando no existe")
    void findByIdThrowsWhenNotFound() {
        when(productRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("create guarda el producto y valida categorías existentes")
    void createAttachesExistingCategories() {
        CategoryEntity category = CategoryEntity.builder().id(5L).name("Laptops").build();
        sampleProduct.getCategories().add(category);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        ProductEntity result = productService.create(sampleProduct);

        assertThat(result).isSameAs(sampleProduct);
        verify(categoryRepository).findById(5L);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("create falla si una categoría no existe")
    void createFailsWhenCategoryMissing() {
        CategoryEntity ghost = CategoryEntity.builder().id(999L).build();
        sampleProduct.getCategories().add(ghost);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(sampleProduct))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("update modifica nombre y descripción")
    void updateModifiesFields() {
        ProductEntity existing = ProductEntity.builder()
                .id(1L).name("Old").description("Old desc").categories(new ArrayList<>()).build();
        ProductEntity updated = ProductEntity.builder()
                .name("New").description("New desc").categories(new ArrayList<>()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        ProductEntity result = productService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("New desc");
    }

    @Test
    @DisplayName("delete elimina el producto cuando existe")
    void deleteRemovesProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        productService.delete(1L);

        verify(productRepository).delete(sampleProduct);
    }

    @Test
    @DisplayName("delete lanza excepción si el producto no existe")
    void deleteThrowsWhenNotFound() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(42L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
