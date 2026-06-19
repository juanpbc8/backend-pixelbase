package com.pixelbase.backend.modules.catalog.service.impl;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtil;
import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.mapper.BrandMapper;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.service.IBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements IBrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandResponse> getAll() {
        return brandRepository.findAllByOrderByNameAsc().stream()
            .map(brandMapper::toResponse)
            .toList();
    }

    @Override
    public List<BrandAdminTableResponse> getAdminTable() {
        return brandRepository.findAllAdminTable();
    }

    @Override
    public BrandResponse getById(Long id) {
        return brandMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    public BrandResponse getBySlug(String slug) {
        return brandRepository.findBySlug(slug)
            .map(brandMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "La marca con el identificador '%s' no existe.",
                slug
            )));
    }

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        String generatedSlug = SlugUtil.toSlug(request.name());

        // Valida la unicidad de forma unificada antes de persistir
        validateBrandUniqueness(request.name(), generatedSlug, null);

        BrandEntity entity = brandMapper.toEntity(request);
        entity.setSlug(generatedSlug);

        return brandMapper.toResponse(brandRepository.save(entity));
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        BrandEntity brand = findByIdOrThrow(id);
        String requestedName = request.name();
        String generatedSlug = SlugUtil.toSlug(requestedName);

        // Si el nombre cambió, ejecuta la validación cruzada usando el nuevo texto
        if (!brand.getName().equalsIgnoreCase(requestedName)) {
            validateBrandUniqueness(requestedName, generatedSlug, id);
            brand.setSlug(generatedSlug);
        }

        // Modificación limpia delegada al MappingTarget de MapStruct
        brandMapper.updateEntityFromRequest(request, brand);

        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BrandEntity brand = findByIdOrThrow(id);
        long productCount = brandRepository.countProductsByBrandId(id);

        // Regla de Negocio: Candado de integridad relacional en cascada
        if (productCount > 0) {
            throw new ConflictException(String.format(
                "No se puede eliminar la marca '%s' (ID: %d) porque tiene %d productos asignados " +
                    "activos en el inventario.",
                brand.getName(),
                brand.getId(),
                productCount
            ));
        }

        brandRepository.delete(brand);
    }

    /**
     * Busca una marca por ID y falla de inmediato si no existe.
     *
     * @param id identificador numérico de la marca
     * @throws ResourceNotFoundException cuando el registro no existe
     */
    private BrandEntity findByIdOrThrow(Long id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "La marca con el identificador '%s' no existe.",
                id
            )));
    }

    /**
     * Válida que el nombre y el slug no estén ya registrados en otra marca,
     * preservando mensajes de conflicto claros para la operación de negocio.
     *
     * @param name      nombre legible recibido en la petición
     * @param slug      slug generado o solicitado para la marca
     * @param currentId identificador actual de la entidad en edición; es nulo
     *                  durante la creación
     * @throws ConflictException cuando el nombre o el slug ya pertenecen a otra
     *                           marca persistida
     */
    private void validateBrandUniqueness(String name, String slug, Long currentId) {
        // 1. Control por similitud exacta de caracteres
        brandRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe una marca registrada con el nombre '%s'.",
                    name
                ));
            }
        });

        // 2. Control de colisión por normalización de URL (Slug)
        brandRepository.findBySlug(slug).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe una marca registrada con un nombre equivalente o similar a '%s'.",
                    slug
                ));
            }
        });
    }
}
