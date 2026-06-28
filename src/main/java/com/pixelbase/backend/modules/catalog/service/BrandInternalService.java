package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.exception.BadRequestException;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.BrandCreateRequest;
import com.pixelbase.backend.modules.catalog.api.admin.dto.response.BrandAdminTableResponse;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.BrandResponse;

import java.util.List;

public interface BrandInternalService {
    /**
     * Obtiene todas las marcas registradas y las ordena alfabéticamente por
     * nombre para su consumo en la vitrina pública.
     */
    List<BrandResponse> getAll();

    /**
     * Obtiene la vista administrativa de marcas con el conteo agregado de
     * productos asociados para alimentar tablas de gestión y evitar N + 1 consultas.
     */
    List<BrandAdminTableResponse> getAdminTable();

    /**
     * Recupera una marca por su identificador interno.
     *
     * @param id identificador numérico de la marca a consultar
     * @throws ResourceNotFoundException cuando no existe una marca con ese ID
     */
    BrandResponse getById(Long id);

    /**
     * Recupera una marca por su slug público.
     *
     * @param slug identificador legible de la marca
     * @throws ResourceNotFoundException cuando no existe una marca con ese slug
     */
    BrandResponse getBySlug(String slug);

    /**
     * Crea una nueva marca, genera su slug automáticamente y persiste la
     * entidad después de validar que no exista otra marca equivalente.
     *
     * @param request datos de entrada de la marca a registrar
     * @throws ConflictException cuando ya existe una marca con el mismo nombre
     *                           o slug normalizado
     */
    BrandResponse create(BrandCreateRequest request);

    /**
     * Actualiza una marca existente, recalcula el slug si el nombre cambia y
     * valida la unicidad de la nueva identidad comercial.
     *
     * @param id      identificador de la marca a actualizar
     * @param request nuevos datos de la marca
     * @throws ResourceNotFoundException cuando no existe una marca con ese ID
     * @throws ConflictException         cuando el nuevo nombre o slug colisiona con
     *                                   otra marca registrada
     */
    BrandResponse update(Long id, BrandCreateRequest request);

    /**
     * Elimina una marca únicamente si no tiene productos activos asociados en
     * el inventario.
     *
     * @param id identificador de la marca a eliminar
     * @throws ResourceNotFoundException cuando no existe una marca con ese ID
     * @throws BadRequestException       cuando la marca todavía tiene productos
     *                                   asociados y no puede eliminarse
     */
    void delete(Long id);
}
