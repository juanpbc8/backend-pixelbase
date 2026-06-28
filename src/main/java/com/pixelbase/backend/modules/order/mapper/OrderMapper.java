package com.pixelbase.backend.modules.order.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.order.api.customer.dto.response.CustomerOrderDetailResponse;
import com.pixelbase.backend.modules.order.api.customer.dto.response.CustomerOrderSummaryResponse;
import com.pixelbase.backend.modules.order.api.web.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.domain.OrderAddressEntity;
import com.pixelbase.backend.modules.order.domain.OrderEntity;
import com.pixelbase.backend.modules.order.domain.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper(config = GlobalMapperConfig.class)
public abstract class OrderMapper {

    @Value("${app.fallback.product-slug}")
    protected String fallbackSlug;

    @Value("${app.fallback.product-image}")
    protected String fallbackImage;

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
    public abstract OrderEntity toOrderEntity(
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
    public abstract OrderItemEntity toOrderItemEntity(ProductSharedDto product, Integer quantity);

    // --- Mapeo Ligero para el Historial ---
    @Mapping(target = "totalItems",
        expression = "java(order.getItems() != null ? order.getItems().size() : 0)")
    public abstract CustomerOrderSummaryResponse toCustomerOrderSummaryResponse(OrderEntity order);

    // --- Mapeo Exhaustivo para el Detalle (Aplanamiento de Objetos de Valor/Relaciones) ---
    @Mapping(target = "address.addressLine", source = "order.orderAddress.addressLine")
    @Mapping(target = "address.department", source = "order.orderAddress.department")
    @Mapping(target = "address.province", source = "order.orderAddress.province")
    @Mapping(target = "address.district", source = "order.orderAddress.district")
    @Mapping(target = "address.reference", source = "order.orderAddress.reference")
    @Mapping(target = "address.contactFirstName", source = "order.orderAddress.contactFirstName")
    @Mapping(target = "address.contactLastName", source = "order.orderAddress.contactLastName")
    @Mapping(target = "address.contactPhone", source = "order.orderAddress.contactPhone")
    @Mapping(target = "items", expression = "java(enrichOrderItems(order.getItems(), catalogMap))")
    public abstract CustomerOrderDetailResponse toCustomerOrderDetailResponse(OrderEntity order,
                                                                              Map<Long, ProductSharedDto> catalogMap);

    /**
     * Pipeline de Enriquecimiento Funcional encapsulado dentro del Mapper.
     * Desacopla al Service de la lógica estructural de manipulación de arrays y multimedia.
     */
    public List<CustomerOrderDetailResponse.CustomerOrderItemResponse> enrichOrderItems(
        List<OrderItemEntity> items, Map<Long, ProductSharedDto> catalogMap) {

        if (items == null) return List.of();

        return items.stream()
            .map(item -> {
                // Recuperación de la caché del mapa O(1)
                ProductSharedDto catalogProduct = (catalogMap != null) ?
                    catalogMap.get(item.getProductId()) : null;

                // Resolución segura de Slugs (Paracaídas anti-404)
                String liveSlug = (catalogProduct != null) ? catalogProduct.slug() : fallbackSlug;

                // Orquestación de la miniatura principal (position = 0)
                String liveImageUrl = fallbackImage;
                if (catalogProduct != null && catalogProduct.images() != null && !catalogProduct.images().isEmpty()) {
                    liveImageUrl = catalogProduct.images().stream()
                        .filter(ProductSharedDto.ProductImageSharedDto::isMain)
                        .map(ProductSharedDto.ProductImageSharedDto::url)
                        .findFirst()
                        .orElse(catalogProduct.images().getFirst().url());
                }

                return new CustomerOrderDetailResponse.CustomerOrderItemResponse(
                    liveSlug,
                    item.getProductNameSnapshot(),
                    item.getQuantity(),
                    item.getPriceSnapshot(),
                    liveImageUrl
                );
            })
            .toList();
    }

    // --- Enganche Bidireccional Automático ---
    @AfterMapping
    protected void linkOrderRelations(@MappingTarget OrderEntity order) {
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }
        if (order.getOrderAddress() != null) {
            order.getOrderAddress().setOrder(order);
        }
    }
}
