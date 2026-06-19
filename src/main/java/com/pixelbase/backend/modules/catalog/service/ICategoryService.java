package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.exception.BadRequestException;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    /**
     * Obtiene el árbol completo de categorías con sus hijos para alimentar la
     * navegación pública y evitar el problema de N+1 consultas.
     */
    List<CategoryResponse> getCategoryTree();

    /**
     * Recupera una categoría y sus subcategorías a partir de su slug público.
     *
     * @param slug identificador legible de la categoría
     * @throws ResourceNotFoundException cuando no existe una categoría con ese slug
     */
    CategoryResponse getBySlug(String slug);

    /**
     * Recupera una categoría por su identificador numérico.
     *
     * @param id identificador de la categoría
     * @throws ResourceNotFoundException cuando no existe una categoría con ese identificador
     */
    CategoryResponse getById(Long id);

    /**
     * Obtiene la lista plana de categorías utilizada por la vista de
     * administración.
     */
    List<CategoryAdminTableResponse> getAdminTable();

    /**
     * Crea una nueva categoría, genera su slug automáticamente y válida la
     * unicidad junto con las reglas de jerarquía.
     *
     * @param request datos de la categoría a crear
     * @throws ConflictException         cuando el nombre o el slug ya pertenecen a otra categoría registrada
     * @throws ResourceNotFoundException cuando la categoría padre indicada no existe
     * @throws BadRequestException       cuando la relación padre/hijo viola las reglas de negocio
     */
    CategoryResponse create(CategoryRequest request);

    /**
     * Actualiza una categoría existente, recalcula el slug cuando cambie el
     * nombre y válida la jerarquía resultante.
     *
     * @param id      identificador de la categoría a actualizar
     * @param request nuevos datos de la categoría
     * @throws ResourceNotFoundException cuando no existe la categoría a actualizar o el padre indicado
     * @throws ConflictException         cuando el nombre o el slug colisionan con otra categoría registrada
     * @throws BadRequestException       cuando la nueva jerarquía viola las reglas de negocio
     */
    CategoryResponse update(Long id, CategoryRequest request);

    /**
     * Elimina una categoría solo si no tiene subcategorías activas ni productos
     * asignados directamente.
     *
     * @param id identificador de la categoría a eliminar
     * @throws ResourceNotFoundException cuando no existe la categoría
     * @throws BadRequestException       cuando la categoría tiene subcategorías o productos asignados
     */
    void delete(Long id);
}
