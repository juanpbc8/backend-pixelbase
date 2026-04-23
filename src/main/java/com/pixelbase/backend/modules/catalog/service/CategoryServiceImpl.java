package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.exception.BadRequestException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtils;
import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.mapper.CategoryMapper;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getCategoryTree() {
        // Usamos el method con JOIN FETCH para evitar muchas consultas a la DB
        return categoryRepository.findAllWithChildren().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id)));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        CategoryEntity entity = categoryMapper.toEntity(request);
        entity.setSlug(SlugUtils.toSlug(request.name())); // Slug automático

        if (request.parentId() != null) {
            CategoryEntity parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría Padre", request.parentId()));
            validateDepth(parent);
            entity.setParent(parent);
        }

        return categoryMapper.toResponse(categoryRepository.save(entity));
    }

    // Lógica para validar profundidad máxima de categorías (máximo 3 niveles)
    private void validateDepth(CategoryEntity parent) {
        int depth = 1;
        CategoryEntity current = parent;

        // Caminamos hacia arriba hasta la raíz para contar el nivel
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }

        if (depth >= 3) {
            throw new BadRequestException("La profundidad máxima de categorías es de 3 niveles");
        }
    }
}