package com.pixelbase.backend.modules.catalog.controller.web;

import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Storefront", description = "Endpoints públicos para navegación de categorías")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/tree")
    @Operation(summary = "Obtener árbol de categorías",
            description = "Devuelve la jerarquía completa de categorías para el Navbar")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }
}