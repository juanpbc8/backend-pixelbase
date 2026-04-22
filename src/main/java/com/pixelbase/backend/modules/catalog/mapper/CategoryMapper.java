package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface CategoryMapper {
    /**
     * Convierte la entidad a respuesta, manejando la recursividad de subcategorías.
     */
    CategoryResponse toResponse(CategoryEntity entity);

    /**
     * Convierte el request a entidad. El parentId se manejará en el Service.
     */
    @Mapping(target = "parent", ignore = true)
    CategoryEntity toEntity(CategoryRequest request);
}