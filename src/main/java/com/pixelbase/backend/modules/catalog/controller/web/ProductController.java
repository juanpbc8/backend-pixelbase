package com.pixelbase.backend.modules.catalog.controller.web;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductResponse;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Catalog - Storefront", description = "Endpoints públicos para visualización de productos")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    @Operation(summary = "Búsqueda y filtrado dinámico de productos",
            description = "Busca productos activos por nombre, categoría, marca y rango de precio con paginación")
    public ResponseEntity<PageResponse<ProductCardResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            // Documenta automáticamente page, size y sort para la paginación y ordenamiento
            @ParameterObject
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getStorefrontProducts(
                search, categoryId, brandId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener detalle por slug",
            description = "Devuelve la información detallada de un producto para su ficha técnica")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }
}