package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.ProductCreateRequest;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.ProductStatusUpdateRequest;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.api.web.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.api.web.dto.response.ProductDetailResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductInternalService {
    /**
     * Obtiene el detalle público del producto por su slug.
     *
     * @param slug identificador semántico único del producto
     * @throws ResourceNotFoundException cuando el producto no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al consultar el producto
     */
    ProductDetailResponse getBySlug(String slug);

    /**
     * Obtiene productos activos para la vitrina con filtros dinámicos y paginación.
     *
     * @param search     texto libre para búsqueda por nombre, sku o número de parte
     * @param categoryId identificador de la categoría para filtrar resultados
     * @param brandId    identificador de la marca para filtrar resultados
     * @param minPrice   precio mínimo permitido
     * @param maxPrice   precio máximo permitido
     * @param pageable   parámetros de paginación y ordenamiento
     * @throws RuntimeException cuando ocurre un error inesperado al consultar el catálogo
     */
    PageResponse<ProductCardResponse> getStorefrontProducts(
        String search, Long categoryId, Long brandId,
        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable
    );

    /**
     * Obtiene el listado administrativo paginado para la grilla de productos.
     *
     * @param search     texto libre de búsqueda
     * @param categoryId identificador de categoría para filtrar
     * @param brandId    identificador de marca para filtrar
     * @param pageable   parámetros de paginación y ordenamiento
     * @throws RuntimeException cuando ocurre un error inesperado al consultar el catálogo
     */
    PageResponse<ProductAdminTableResponse> getAdminProducts(
        String search, Long categoryId, Long brandId, Pageable pageable
    );

    /**
     * Obtiene el detalle administrativo completo de un producto.
     *
     * @param id identificador del producto
     * @throws ResourceNotFoundException cuando el producto no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al consultar el producto
     */
    ProductAdminDetailResponse getAdminById(Long id);

    /**
     * Crea un producto nuevo con SKU corporativo, slug y relaciones completas.
     *
     * @param request datos de entrada del producto
     * @throws ConflictException         cuando el nombre, slug o número de parte ya existen
     * @throws ResourceNotFoundException cuando la marca o categoría no existen
     * @throws RuntimeException          cuando ocurre un error inesperado al crear el producto
     */
    ProductAdminDetailResponse create(ProductCreateRequest request);

    /**
     * Actualiza un producto existente preservando las reglas de unicidad.
     *
     * @param id      identificador del producto a actualizar
     * @param request datos de entrada del producto
     * @throws ConflictException         cuando el nombre, slug o número de parte ya existen
     * @throws ResourceNotFoundException cuando el producto, marca o categoría no existen
     * @throws RuntimeException          cuando ocurre un error inesperado al actualizar el producto
     */
    ProductAdminDetailResponse update(Long id, ProductCreateRequest request);

    /**
     * Actualiza el estado de visibilidad del producto.
     *
     * @param id      identificador del producto
     * @param request estado solicitado para la transición
     * @throws ResourceNotFoundException cuando el producto no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al actualizar el estado
     */
    void updateStatus(Long id, ProductStatusUpdateRequest request);
}
