package com.pixelbase.backend.modules.catalog.controller.admin;

import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog - Admin", description = "Panel de gestión de categorías")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría",
            description = "Registra una nueva categoría validando un máximo de 3 niveles de profundidad")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(categoryService.create(request), HttpStatus.CREATED);
    }
}