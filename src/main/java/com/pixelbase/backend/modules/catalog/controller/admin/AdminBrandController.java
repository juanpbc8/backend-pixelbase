package com.pixelbase.backend.modules.catalog.controller.admin;

import com.pixelbase.backend.common.exception.ApiError;
import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Catalogo - Admin - Marcas",
    description = "Panel de gestión de marcas"
)
public class AdminBrandController {

    private final IBrandService brandService;

    @GetMapping
    @Operation(
        summary = "Listar marcas para administración",
        description = "Devuelve una grilla administrativa con conteo de productos asociados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación exitosa.")
    })
    public ResponseEntity<List<BrandAdminTableResponse>> getAdminTable() {
        return ResponseEntity.ok(brandService.getAdminTable());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener marca por identificador",
        description = "Recupera la información completa de una marca usando su ID interno."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación exitosa."),
        @ApiResponse(
            responseCode = "404",
            description = "La marca con el identificador solicitado no existe.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<BrandResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getById(id));
    }

    @PostMapping
    @Operation(
        summary = "Registrar una nueva marca",
        description = "Crea una marca y devuelve la ubicación del recurso recién generado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Marca creada exitosamente."),
        @ApiResponse(
            responseCode = "400",
            description = "La petición es inválida por fallos de validación.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "La marca con el identificador solicitado no existe.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una marca con el mismo nombre o slug.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        BrandResponse created = brandService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar marca existente",
        description = "Modifica una marca por su ID y regenera el slug si cambia el nombre."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación exitosa."),
        @ApiResponse(
            responseCode = "400",
            description = "La petición es inválida por fallos de validación.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "La marca con el identificador solicitado no existe.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una marca con el mismo nombre o slug.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<BrandResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody BrandRequest request
    ) {
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar marca",
        description = "Elimina una marca si no tiene productos activos asociados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Marca eliminada exitosamente."),
        @ApiResponse(
            responseCode = "400",
            description = "La marca no puede eliminarse porque tiene productos asignados.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "La marca con el identificador solicitado no existe.",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
