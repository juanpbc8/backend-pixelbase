package com.pixelbase.backend.modules.catalog.controller.web;

import com.pixelbase.backend.common.exception.ApiError;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Storefront - Categorías",
    description = "Endpoints públicos para navegación de categorías")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "Obtener árbol jerárquico de categorías",
        description = "Devuelve la taxonomía completa y anidada de la tienda en una sola consulta. Se " +
            "utiliza para renderizar los menús de navegación global y vitrinas públicas de hardware.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa.")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener detalle de categoría por slug",
        description = "Recupera la información de una categoría específica y sus subcategorías directas. Se" +
            " utiliza para construir los filtros laterales de navegación en la vitrina de productos y " +
            "generar las migas de pan (breadcrumbs).")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa"),
        @ApiResponse(
            responseCode = "404",
            description = "La categoría solicitada no existe en el sistema",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }
}
