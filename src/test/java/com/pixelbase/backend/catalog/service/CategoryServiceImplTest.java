package com.pixelbase.backend.catalog.service;

import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.repository.CategoryRepository;
import com.pixelbase.backend.catalog.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryEntity sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = CategoryEntity.builder()
                .id(1L)
                .name("Laptops")
                .build();
    }

    @Test
    @DisplayName("findAll devuelve todas las categorías")
    void findAllReturnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<CategoryEntity> result = categoryService.findAll();

        assertThat(result).hasSize(1).containsExactly(sampleCategory);
    }

    @Test
    @DisplayName("findById devuelve la categoría cuando existe")
    void findByIdReturnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        CategoryEntity result = categoryService.findById(1L);

        assertThat(result.getName()).isEqualTo("Laptops");
    }

    @Test
    @DisplayName("findById lanza excepción cuando no existe")
    void findByIdThrowsWhenNotFound() {
        when(categoryRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(42L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("create guarda una nueva categoría")
    void createSavesCategory() {
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);

        CategoryEntity result = categoryService.create(sampleCategory);

        assertThat(result).isSameAs(sampleCategory);
        verify(categoryRepository).save(sampleCategory);
    }

    @Test
    @DisplayName("update cambia el nombre de la categoría existente")
    void updateModifiesName() {
        CategoryEntity incoming = CategoryEntity.builder().name("Laptops Gaming").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);

        CategoryEntity result = categoryService.update(1L, incoming);

        assertThat(result.getName()).isEqualTo("Laptops Gaming");
    }

    @Test
    @DisplayName("delete elimina la categoría cuando existe")
    void deleteRemovesCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        categoryService.delete(1L);

        verify(categoryRepository).delete(sampleCategory);
    }
}
