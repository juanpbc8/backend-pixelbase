package com.pixelbase.backend.modules.catalog.controller.web;

import com.pixelbase.backend.common.exception.ApiError;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.service.IBrandService;
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
@RequestMapping("/api/v1/public/brands")
@RequiredArgsConstructor
@Tag(
    name = "Catalogo - Storefront - Marcas",
    description = "Endpoints públicos para navegación de marcas"
)
public class BrandController {

    private final IBrandService brandService;

    @GetMapping
    @Operation(
        summary = "Listar marcas públicas",
        description = "Devuelve todas las marcas disponibles para la vitrina pública."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa."
        )
    })
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandService.getAll());
    }

    @GetMapping("/{slug}")
    @Operation(
        summary = "Obtener marca por slug",
        description = "Recupera el detalle de una marca específica usando su slug público."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operación exitosa"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "La marca solicitada no existe en el sistema",
            content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<BrandResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(brandService.getBySlug(slug));
    }
}
