package com.pixelbase.backend.modules.configuration.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.configuration.domain.StoreEntity;
import com.pixelbase.backend.modules.configuration.dto.response.StoreResponse;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface StoreMapper {
    StoreResponse toResponse(StoreEntity entity);

    List<StoreResponse> toResponseList(List<StoreEntity> entities);

    StoreSharedDto toSharedDto(StoreEntity entity);
}
