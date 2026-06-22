package com.pixelbase.backend.modules.order.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import com.pixelbase.backend.modules.order.domain.OrderAddressEntity;
import com.pixelbase.backend.modules.order.dto.request.OrderCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderAddressMapper {
    // --- Caso 1: A_DOMICILIO (Une los DTOs de dirección y receptor del request) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "addressLine", source = "address.addressLine")
    @Mapping(target = "department", source = "address.department")
    @Mapping(target = "province", source = "address.province")
    @Mapping(target = "district", source = "address.district")
    @Mapping(target = "reference", source = "address.reference")
    @Mapping(target = "contactFirstName", source = "recipient.firstName")
    @Mapping(target = "contactLastName", source = "recipient.lastName")
    @Mapping(target = "contactPhone", source = "recipient.phone")
    OrderAddressEntity toHomeDeliveryAddress(OrderCreateRequest.AddressRequest address,
                                             OrderCreateRequest.RecipientRequest recipient);

    // --- Caso 2: RECOJO_EN_TIENDA (Mezcla la Sede de Configuración con el receptor del request) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "addressLine", source = "store.addressLine")
    @Mapping(target = "department", source = "store.department")
    @Mapping(target = "province", source = "store.province")
    @Mapping(target = "district", source = "store.district")
    @Mapping(target = "reference", constant = "Recojo en mostrador oficial de la sede")
    @Mapping(target = "contactFirstName", source = "recipient.firstName")
    @Mapping(target = "contactLastName", source = "recipient.lastName")
    @Mapping(target = "contactPhone", source = "recipient.phone")
    OrderAddressEntity toStorePickupAddress(StoreSharedDto store,
                                            OrderCreateRequest.RecipientRequest recipient);
}
