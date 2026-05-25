package com.pixelbase.backend.modules.catalog.mapper;

import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.common.dto.AuditResponse;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductImageEntity;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductDetailResponse;
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
     * Respuesta detallada para el Admin o Vista de Producto.
     * Mapea automáticamente colecciones y objetos anidados.
     */
    ProductDetailResponse toResponse(ProductEntity entity);

    /**
     * Respuesta optimizada para tarjetas del Storefront (Angular).
     * Incluye stock y lógica para extraer la imagen principal.
     */
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "mainImageUrl", expression = "java(mapMainImage(entity))")
    ProductCardResponse toCardResponse(ProductEntity entity);

    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryName", source = "category.name")
    ProductAdminTableResponse toAdminTableResponse(ProductEntity entity);

    @Mapping(target = "audit", source = "entity", qualifiedByName = "mapAudit")
    ProductAdminDetailResponse toAdminDetailResponse(ProductEntity entity);

    /**
     * Convierte el request de creación a entidad.
     * Las relaciones de ID (brandId, categoryId) se resuelven en el Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    ProductEntity toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget ProductEntity entity);

    /**
     * Lógica personalizada para obtener la imagen con posición 0 (principal).
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

    @Named("mapAudit")
    default AuditResponse toAuditResponse(ProductEntity entity) {
        return new AuditResponse(
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getCreatedBy(),
            entity.getUpdatedBy()
        );
    }

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
