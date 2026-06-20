package com.pixelbase.backend.modules.order.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.order.domain.OrderAddressEntity;
import com.pixelbase.backend.modules.order.domain.OrderEntity;
import com.pixelbase.backend.modules.order.domain.OrderItemEntity;
import com.pixelbase.backend.modules.order.dto.request.OrderCreateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderMapper {

    // --- Mapeo de la Cabecera utilizando Múltiples Fuentes ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "status", constant = "PENDIENTE")
    @Mapping(target = "customerFirstName", source = "request.customer.firstName")
    @Mapping(target = "customerLastName", source = "request.customer.lastName")
    @Mapping(target = "customerEmail", source = "request.customer.email")
    @Mapping(target = "customerPhone", source = "request.customer.phone")
    @Mapping(target = "customerDocType", source = "request.customer.docType")
    @Mapping(target = "customerDocNumber", source = "request.customer.docNumber")
    // Mapea la lista y dirección ya procesada
    @Mapping(target = "items", source = "items")
    @Mapping(target = "orderAddress", source = "orderAddress")
    OrderEntity toOrderEntity(
        OrderCreateRequest request,
        String orderCode,
        BigDecimal totalPrice,
        Long userId,
        List<OrderItemEntity> items,
        OrderAddressEntity orderAddress
    );

    // --- Mapeo de los Ítems (Snapshots) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "priceSnapshot", source = "product.price")
    @Mapping(target = "productNameSnapshot", source = "product.name")
    @Mapping(target = "skuSnapshot", source = "product.sku")
    @Mapping(target = "partNumberSnapshot", source = "product.partNumber")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemEntity toItemEntity(ProductSharedDto product, Integer quantity);

    // --- Enganche Bidireccional Automático ---
    @AfterMapping
    default void linkOrderRelations(@MappingTarget OrderEntity order) {
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }
        if (order.getOrderAddress() != null) {
            order.getOrderAddress().setOrder(order);
        }
    }
}
