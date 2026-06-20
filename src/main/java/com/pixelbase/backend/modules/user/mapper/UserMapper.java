package com.pixelbase.backend.modules.user.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface UserMapper {

    UserAuthDto toAuthDto(UserEntity user);
}
