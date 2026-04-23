package com.pixelbase.backend.modules.catalog.controller.admin;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductResponse;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Catalog - Admin", description = "Panel de gestión de inventario y productos")
public class AdminProductController {

    private final IProductService productService;

    @GetMapping
    @Operation(summary = "Listado administrativo paginado",
            description = "Permite al administrador buscar y gestionar todos los productos (Activos e Inactivos)")
    public ResponseEntity<PageResponse<ProductResponse>> getAdminProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getAdminProducts(search, categoryId, brandId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar producto",
            description = "Registra un producto con SKU único y especificaciones JSON")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto",
            description = "Modifica los datos de un producto existente por su ID")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de visibilidad",
            description = "Cambia el estado del producto (ACTIVE/INACTIVE) para mostrarlo u ocultarlo de la tienda")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusRequest request) {
        productService.updateStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}