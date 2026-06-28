package com.pixelbase.backend.modules.configuration.api.web;

import com.pixelbase.backend.modules.configuration.api.web.dto.response.StoreResponse;
import com.pixelbase.backend.modules.configuration.service.StoreInternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/stores")
@RequiredArgsConstructor
@Tag(name = "Storefront - Tiendas", description = "Listado de sedes físicas y puntos de recojo")
public class StoreController {

    private final StoreInternalService storeInternalService;

    @GetMapping
    @Operation(
        summary = "Listar tiendas operativas",
        description = "Retorna una lista simplificada de todas las sedes habilitadas para la opción de " +
            "recojo en tienda física."
    )
    public ResponseEntity<List<StoreResponse>> getActiveStores() {
        return ResponseEntity.ok(storeInternalService.getActiveStoresForStorefront());
    }
}
