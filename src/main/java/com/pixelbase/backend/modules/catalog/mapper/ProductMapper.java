package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.common.dto.AuditResponse;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.ProductCreateRequest;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.api.web.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.api.web.dto.response.ProductDetailResponse;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductImageEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import org.mapstruct.*;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        BrandMapper.class,
        CategoryMapper.class
    },
    collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED
)
public interface ProductMapper {

    /**
     * Convierte una entidad de producto en su respuesta detallada.
     *
     * @param entity entidad origen del producto
     */
    ProductDetailResponse toResponse(ProductEntity entity);

    /**
     * Convierte una entidad de producto en una tarjeta resumida para la vitrina.
     *
     * @param entity entidad origen del producto
     */
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "mainImageUrl", expression = "java(mapMainImage(entity))")
    ProductCardResponse toCardResponse(ProductEntity entity);

    /**
     * Convierte una entidad de producto al resumen administrativo de tabla.
     *
     * @param entity entidad origen del producto
     */
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryName", source = "category.name")
    ProductAdminTableResponse toAdminTableResponse(ProductEntity entity);

    /**
     * Convierte una entidad de producto al detalle administrativo completo.
     *
     * @param entity entidad origen del producto
     */
    @Mapping(target = "audit", source = "entity", qualifiedByName = "mapAudit")
    ProductAdminDetailResponse toAdminDetailResponse(ProductEntity entity);

    /**
     * Convierte el request de creación a entidad de dominio.
     *
     * @param request datos de entrada del producto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    ProductEntity toEntity(ProductCreateRequest request);

    @Mapping(target = "active", expression = "java(mapStatusToActive(entity))")
    ProductSharedDto toSharedDto(ProductEntity entity);

    /**
     * Actualiza una entidad existente a partir del request recibido.
     *
     * @param request datos de entrada del producto
     * @param entity  entidad de destino a actualizar
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateEntityFromRequest(ProductCreateRequest request, @MappingTarget ProductEntity entity);

    /**
     * Obtiene la URL principal de imagen para la tarjeta del producto.
     *
     * @param product entidad del producto evaluada
     * @return URL principal o nula si no hay imágenes
     */
    @Named("mapMainImage")
    default String mapMainImage(ProductEntity product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
            .filter(img -> img.getPosition() == null || img.getPosition() == 0)
            .map(ProductImageEntity::getUrl)
            .findFirst()
            .orElse(product.getImages().getFirst().getUrl()); // Si no hay posición 0, toma la primera
    }

    /**
     * Construye el bloque de auditoría para las respuestas administrativas.
     *
     * @param entity entidad origen del producto
     * @return bloque de auditoría con fechas y usuarios
     */
    @Named("mapAudit")
    default AuditResponse toAuditResponse(ProductEntity entity) {
        return new AuditResponse(
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

    @Named("mapStatusToActive")
    default boolean mapStatusToActive(ProductEntity entity) {
        return entity.getStatus() == ProductStatus.ACTIVO;
    }

    /**
     * Sincroniza la relación bidireccional del producto con sus imágenes y genera un altText si es null o
     * está vacío, usando el formato "{nombre del producto} - Imagen {número de imagen}".
     *
     * @param product entidad de producto ya mapeada
     */
    @AfterMapping
    default void establishBidirectionalRelation(@MappingTarget ProductEntity product) {
        if (product.getImages() == null) {
            return;
        }
        int position = 0;
        for (ProductImageEntity image : product.getImages()) {
            image.setProduct(product);
            image.setPosition(position);
            if (image.getAltText() == null || image.getAltText().isBlank()) {
                image.setAltText(String.format(
                    "%s - Imagen %d", product.getName(), position + 1));
            }
            position++;
        }
    }
}
