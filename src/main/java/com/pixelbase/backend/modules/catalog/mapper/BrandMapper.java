package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface BrandMapper {
    BrandResponse toResponse(BrandEntity entity);

    BrandEntity toEntity(BrandRequest request);
}