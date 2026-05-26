package com.pixelbase.backend.modules.catalog.controller.admin;

import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.service.ICategoryService;
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
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Admin - Categorías",
    description = "Panel de gestión de categorías")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @Operation(summary = "Registrar una nueva categoría",
        description = "Crea una categoría en el sistema. Si no se envía un ID padre, se registra como raíz.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Categoría creada exitosamente"),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una categoría con el mismo nombre o slug.")
    })
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar categorías para el panel de gestión",
        description = "Devuelve una lista plana de todas las categorías registradas, enriquecida con " +
            "métricas de control como el nivel de profundidad actual y la cantidad de productos asociados. " +
            "Diseñado para poblar tablas de administración de datos.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK")
    })
    public ResponseEntity<List<CategoryAdminTableResponse>> getAdminTable() {
        return ResponseEntity.ok(categoryService.getAdminTable());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID para edición",
        description = "Recupera la metadata completa de una categoría mediante su clave primaria Se utiliza" +
            " para inicializar formularios de edición y permitir que la interfaz valide preventivamente si " +
            "el nodo tiene hijos antes de intentar modificar su posición.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK"),
    })
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría existente",
        description = "Modifica una categoría por su ID y regenera el slug si cambia de nombre.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK"),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una categoría con el mismo nombre o slug.")
    })
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría",
        description = "Remueve de forma definitiva una categoría del sistema por su ID.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Categoría eliminada con éxito"),
        @ApiResponse(
            responseCode = "409",
            description = "La categoría no se puede eliminar porque tiene subcategorías o productos "
                + "asociados.")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
