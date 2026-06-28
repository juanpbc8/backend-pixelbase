package com.pixelbase.backend.modules.catalog.api.admin;

import com.pixelbase.backend.modules.catalog.api.admin.dto.request.BrandCreateRequest;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.BrandAdminTableResponse;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.service.BrandInternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(
    name = "Catalogo - Admin - Marcas",
    description = "Panel de gestión de marcas"
)
public class AdminBrandController {

    private final BrandInternalService brandInternalService;

    @GetMapping
    @Operation(
        summary = "Listar marcas para administración",
        description = "Devuelve una grilla administrativa con conteo de productos asociados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación exitosa.")
    })
    public ResponseEntity<List<BrandAdminTableResponse>> getAdminTable() {
        return ResponseEntity.ok(brandInternalService.getAdminTable());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener marca por identificador",
        description = "Recupera la información completa de una marca usando su ID interno."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación exitosa."),
    })
    public ResponseEntity<BrandResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(brandInternalService.getById(id));
    }

    @PostMapping
    @Operation(
        summary = "Registrar una nueva marca",
        description = "Crea una marca y devuelve la ubicación del recurso recién generado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Marca creada exitosamente."),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una marca con el mismo nombre o slug."
        )
    })
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandCreateRequest request) {
        BrandResponse created = brandInternalService.create(request);
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
            responseCode = "409",
            description = "Ya existe una marca con el mismo nombre o slug."
        )
    })
    public ResponseEntity<BrandResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody BrandCreateRequest request
    ) {
        return ResponseEntity.ok(brandInternalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar marca",
        description = "Elimina una marca si no tiene productos activos asociados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Marca eliminada exitosamente."),
        @ApiResponse(
            responseCode = "409",
            description = "La marca no puede eliminarse porque tiene productos activos asociados."
        )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandInternalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
