package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.BrandCreateRequest;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface BrandMapper {
    BrandResponse toResponse(BrandEntity entity);

    BrandEntity toEntity(BrandCreateRequest request);

    // CANDADO CLEAN CODE: Permite actualizar la entidad existente directamente desde el Request
    void updateEntityFromRequest(BrandCreateRequest request, @MappingTarget BrandEntity entity);
}
