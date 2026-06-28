package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.CategoryCreateRequest;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
    CategoryEntity toEntity(CategoryCreateRequest request);

    void updateEntityFromRequest(CategoryCreateRequest request, @MappingTarget CategoryEntity entity);
}
