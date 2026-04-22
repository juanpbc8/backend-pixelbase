package com.pixelbase.backend.modules.catalog.mapper;


import com.pixelbase.backend.common.config.GlobalMapperConfig;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductImageEntity;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class, uses = {BrandMapper.class, CategoryMapper.class})
public interface ProductMapper {

    /**
     * Respuesta detallada para el Admin o Vista de Producto.
     * Mapea automáticamente colecciones y objetos anidados.
     */
    ProductResponse toResponse(ProductEntity entity);

    /**
     * Respuesta optimizada para tarjetas del Storefront (Angular).
     * Incluye stock y lógica para extraer la imagen principal.
     */
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "mainImageUrl", source = "images", qualifiedByName = "mapMainImage")
    ProductCardResponse toCardResponse(ProductEntity entity);

    /**
     * Convierte el request de creación a entidad.
     * Las relaciones de ID (brandId, categoryId) se resuelven en el Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "slug", ignore = true)
    // El slug se genera automáticamente en el Service
    ProductEntity toEntity(ProductRequest request);

    /**
     * Lógica personalizada para obtener la imagen con posición 0 (principal).
     */
    @Named("mapMainImage")
    default String mapMainImage(List<ProductImageEntity> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> img.getPosition() == 0)
                .map(ProductImageEntity::getUrl)
                .findFirst()
                .orElse(images.getFirst().getUrl()); // Si no hay posición 0, toma la primera
    }
}