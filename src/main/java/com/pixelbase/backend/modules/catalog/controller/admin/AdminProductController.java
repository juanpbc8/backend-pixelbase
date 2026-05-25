package com.pixelbase.backend.modules.catalog.controller.admin;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ApiError;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catalogo - Admin - Productos",
    description = "Panel de gestión de inventario y productos")
public class AdminProductController {

    private final IProductService productService;

    @GetMapping
    @Operation(summary = "Listado administrativo paginado",
        description = "Permite al administrador buscar y gestionar todos los productos (Activos e Inactivos)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa")
    })
    public ResponseEntity<PageResponse<ProductAdminTableResponse>> getAdminProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long brandId,
        @ParameterObject
        @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getAdminProducts(search, categoryId, brandId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID",
        description = "Recupera el detalle completo para edición administrativa")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa"),
        @ApiResponse(
            responseCode = "404",
            description = "El producto solicitado no existe",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductAdminDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getAdminById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar producto",
        description = "Registra un producto con SKU único y especificaciones JSON")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente"),
        @ApiResponse(
            responseCode = "400",
            description = "La petición contiene campos inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe un producto con el mismo nombre o número de parte",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductAdminDetailResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductAdminDetailResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto",
        description = "Modifica los datos de un producto existente por su ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa"),
        @ApiResponse(
            responseCode = "400",
            description = "La petición contiene campos inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "El producto solicitado no existe",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe un producto con el mismo nombre o número de parte",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ProductAdminDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de visibilidad",
        description = "Cambia el estado del producto (ACTIVO/INACTIVO) para mostrarlo u ocultarlo de la "
            + "tienda")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Estado actualizado"),
        @ApiResponse(
            responseCode = "400",
            description = "La petición contiene campos inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "404",
            description = "El producto solicitado no existe",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody ProductStatusRequest request
    ) {
        productService.updateStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}
