package com.pixelbase.backend.modules.catalog.controller.web;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ApiError;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductDetailResponse;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Storefront - Productos",
    description = "Endpoints públicos para visualización de productos")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    @Operation(summary = "Búsqueda y filtrado dinámico de productos",
        description = "Busca productos activos por nombre, categoría, marca y rango de precio con paginación")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa")
    })
    public ResponseEntity<PageResponse<ProductCardResponse>> getProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long brandId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        // Documenta automáticamente page, size y sort para la paginación y ordenamiento
        @ParameterObject
        @PageableDefault(size = 12, sort = "updatedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getStorefrontProducts(
            search, categoryId, brandId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener detalle por slug",
        description = "Devuelve la información detallada de un producto para su ficha técnica")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa"),
        @ApiResponse(
            responseCode = "404",
            description = "El producto solicitado no existe",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductDetailResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }
}
