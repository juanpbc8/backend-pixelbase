package com.pixelbase.backend.modules.user.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.user.api.customer.dto.request.CustomerAddressSaveRequest;
import com.pixelbase.backend.modules.user.api.customer.dto.request.CustomerProfileUpdateRequest;
import com.pixelbase.backend.modules.user.api.customer.dto.response.CustomerAddressResponse;
import com.pixelbase.backend.modules.user.api.customer.dto.response.CustomerProfileResponse;
import com.pixelbase.backend.modules.user.domain.UserAddressEntity;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface CustomerMapper {
    // --- Mapeos de Perfil ---
    CustomerProfileResponse toProfileResponse(UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    void updateEntityFromRequest(CustomerProfileUpdateRequest request, @MappingTarget UserEntity user);

    // --- Mapeos de Direcciones Limpias (Sin fricción de datos de contacto) ---
    @Mapping(target = "isDefault", source = "defaulted")
    CustomerAddressResponse toAddressResponse(UserAddressEntity address);

    List<CustomerAddressResponse> toAddressResponseList(List<UserAddressEntity> addresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaulted", source = "isDefault")
    UserAddressEntity toAddressEntity(CustomerAddressSaveRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaulted", source = "isDefault")
    void updateAddressEntityFromRequest(CustomerAddressSaveRequest request,
                                        @MappingTarget UserAddressEntity address);
}
